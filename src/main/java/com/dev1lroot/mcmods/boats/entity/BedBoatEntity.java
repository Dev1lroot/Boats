/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
import net.minecraft.world.level.storage.LevelData;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BedBoatEntity extends Boat {

    private static final Logger LOG = LoggerFactory.getLogger("boats/BedBoatEntity");

    private final Set<UUID> linkedPlayers = new HashSet<>();
    private int tickCounter = 0;

    public BedBoatEntity(EntityType<? extends BedBoatEntity> type, Level level, Supplier<Item> boatItem) {
        super(type, level, boatItem);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ValueOutput.TypedOutputList<UUID> list = output.list("linked_players", UUIDUtil.CODEC);
        for (UUID uuid : linkedPlayers) {
            list.add(uuid);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        linkedPlayers.clear();
        for (UUID uuid : input.listOrEmpty("linked_players", UUIDUtil.CODEC)) {
            linkedPlayers.add(uuid);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        for (Entity passenger : List.copyOf(getPassengers())) {
            if (passenger instanceof ServerPlayer sp) {
                if (sp.isSleeping()) {
                    BlockPos boatPos = this.blockPosition();
                    sp.setSleepingPos(boatPos);
                    ServerPlayer.RespawnConfig current = sp.getRespawnConfig();
                    if (current == null || !current.respawnData().pos().equals(boatPos)) {
                        sp.setRespawnPosition(
                            new ServerPlayer.RespawnConfig(
                                LevelData.RespawnData.of(((ServerLevel) level()).dimension(), boatPos, sp.getYRot(), sp.getXRot()),
                                false
                            ),
                            false
                        );
                    }
                } else {
                    passenger.stopRiding();
                }
            }
        }

        // Every 10 ticks: push current position to all online players linked to this boat.
        if (++tickCounter % 10 == 0) {
            ServerLevel serverLevel = (ServerLevel) level();
            BlockPos currentPos = this.blockPosition();
            linkedPlayers.removeIf(playerUUID -> {
                ServerPlayer sp = serverLevel.getServer().getPlayerList().getPlayer(playerUUID);
                if (sp == null) return false; // offline — keep, can't update now
                Optional<UUID> linked = sp.getData(com.dev1lroot.mcmods.boats.Boats.BED_BOAT_UUID.get());
                if (linked.isEmpty() || !linked.get().equals(this.getUUID())) {
                    return true; // player re-linked to a different boat — remove
                }
                sp.setRespawnPosition(
                    new ServerPlayer.RespawnConfig(
                        LevelData.RespawnData.of(serverLevel.dimension(), currentPos, sp.getYRot(), sp.getXRot()),
                        false
                    ),
                    false
                );
                return false;
            });
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        // Only sleeping players may mount — prevents normal riding while still
        // allowing the sleep mechanic to seat the player after startSleepInBed.
        return passenger instanceof Player p && p.isSleeping();
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

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        // Always set spawn on right-click (except in dimensions where beds explode).
        // Store the UUID so the respawn mixin can find this entity even if it drifts.
        serverPlayer.setData(com.dev1lroot.mcmods.boats.Boats.BED_BOAT_UUID.get(), Optional.of(this.getUUID()));
        linkedPlayers.add(serverPlayer.getUUID());
        LOG.info("[BedBoat] Stored UUID {} for player {}, linkedPlayers={}", this.getUUID(), serverPlayer.getName().getString(), linkedPlayers.size());
        serverPlayer.setRespawnPosition(
            new ServerPlayer.RespawnConfig(
                LevelData.RespawnData.of(serverLevel.dimension(), this.blockPosition(), serverPlayer.getYRot(), serverPlayer.getXRot()),
                false
            ),
            true
        );
        LOG.info("[BedBoat] setRespawnPosition to {} for player {}", this.blockPosition(), serverPlayer.getName().getString());

        if (!bedRule.canSleep(serverLevel)) {
            Component msg = bedRule.asProblem().message();
            player.sendSystemMessage(msg != null ? msg : Component.translatable("block.minecraft.bed.no_sleep"));
            return InteractionResult.SUCCESS;
        }

        if (player.isSleeping()) {
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

        if (result.right().isPresent()) {
            // Mount the player so the boat doesn't drift and the sleeping pos stays valid.
            // canAddPassenger allows this because the player is now sleeping.
            // tick() will keep sleepingPos and respawnConfig synced as the boat moves, and dismount on wake-up.
            player.startRiding(this);
        }

        return result.left().isPresent() ? InteractionResult.FAIL : InteractionResult.SUCCESS;
    }
}
