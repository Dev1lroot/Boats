/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.item;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FunctionalBoatItem extends Item {

    private final Supplier<? extends EntityType<? extends AbstractBoat>> entityTypeSupplier;

    public FunctionalBoatItem(Supplier<? extends EntityType<? extends AbstractBoat>> entityTypeSupplier, Item.Properties properties) {
        super(properties);
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }

        Vec3 viewVector = player.getViewVector(1.0f);
        List<Entity> entities = level.getEntities(
            player,
            player.getBoundingBox().expandTowards(viewVector.scale(5.0)).inflate(1.0),
            EntitySelector.CAN_BE_PICKED
        );

        if (!entities.isEmpty()) {
            Vec3 eyePos = player.getEyePosition();
            for (Entity entity : entities) {
                AABB bb = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (bb.contains(eyePos)) {
                    return InteractionResult.PASS;
                }
            }
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            AbstractBoat boat = spawnBoat(level, hitResult, itemStack, player);
            if (boat == null) {
                return InteractionResult.FAIL;
            }
            boat.setYRot(player.getYRot());
            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                level.addFreshEntity(boat);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
                itemStack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private @Nullable AbstractBoat spawnBoat(Level level, HitResult hitResult, net.minecraft.world.item.ItemStack itemStack, Player player) {
        AbstractBoat boat = entityTypeSupplier.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (boat != null) {
            Vec3 location = hitResult.getLocation();
            boat.setInitialPos(location.x, location.y, location.z);
            if (level instanceof ServerLevel serverLevel) {
                EntityType.<AbstractBoat>createDefaultStackConfig(serverLevel, itemStack, player).apply(boat);
            }
        }
        return boat;
    }
}
