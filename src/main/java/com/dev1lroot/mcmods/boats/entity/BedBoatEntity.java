/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;
import net.minecraft.world.phys.Vec3;

public class BedBoatEntity extends Boat {

    public BedBoatEntity(EntityType<? extends BedBoatEntity> type, Level level, Supplier<Item> boatItem) {
        super(type, level, boatItem);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitVec) {
        // Allow leashing (Entity.interact handles leads). Same non-ridable pattern as DoubleChestBoat:
        // ignore client-side false SUCCESS from AbstractBoat's unchecked riding prediction.
        InteractionResult superResult = super.interact(player, hand, hitVec);
        if (superResult != InteractionResult.PASS
                && !(level().isClientSide() && !player.isSecondaryUseActive())) {
            return superResult;
        }

        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level();

        // Check dimension-specific bed rules (handles Nether explosion, End, etc.)
        BedRule bedRule = serverLevel.environmentAttributes()
            .getValue(EnvironmentAttributes.BED_RULE, this.blockPosition());

        if (bedRule.explodes()) {
            serverLevel.explode(
                null,
                serverLevel.damageSources().badRespawnPointExplosion(this.position()),
                null,
                getX(), getEyeY(), getZ(),
                5.0f, true,
                Level.ExplosionInteraction.BLOCK
            );
            return InteractionResult.SUCCESS;
        }

        if (!bedRule.canSleep(serverLevel)) {
            Component msg = bedRule.asProblem().message();
            player.sendSystemMessage(msg != null ? msg : Component.translatable("block.minecraft.bed.no_sleep"));
            return InteractionResult.SUCCESS;
        }

        if (player.isSleeping()) {
            return InteractionResult.FAIL;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        // Attempt sleep — NeoForge's patch to startSleepInBed returns success for non-bed blocks
        // (blocks without HorizontalDirectionalBlock.FACING property), so water / air at entity pos works
        var result = serverPlayer.startSleepInBed(this.blockPosition());

        result.ifLeft(problem -> {
            if (problem != null && problem.message() != null) {
                player.sendSystemMessage(problem.message());
            }
        });

        return result.left().isPresent() ? InteractionResult.FAIL : InteractionResult.SUCCESS;
    }
}
