/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.mixin;

import com.dev1lroot.mcmods.boats.Boats;
import com.dev1lroot.mcmods.boats.entity.BedBoatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    private static final Logger LOG = LoggerFactory.getLogger("boats/ServerPlayerMixin");

    @Redirect(
        method = "findRespawnPositionAndUseSpawnBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/portal/TeleportTransition;missingRespawnBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"
        )
    )
    private TeleportTransition boats$interceptMissingRespawnBlock(
            ServerPlayer player,
            TeleportTransition.PostTeleportTransition postTeleportTransition
    ) {
        LOG.info("[BedBoat] interceptMissingRespawnBlock called for player={}", player.getName().getString());

        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        if (config == null) {
            LOG.info("[BedBoat] RespawnConfig is null — falling back to missing block");
            return TeleportTransition.missingRespawnBlock(player, postTeleportTransition);
        }

        BlockPos storedPos = config.respawnData().pos();
        ResourceKey<Level> storedDim = config.respawnData().dimension();
        LOG.info("[BedBoat] storedPos={} storedDim={}", storedPos, storedDim);

        var server = player.level().getServer();
        if (server == null) {
            LOG.info("[BedBoat] server is null — falling back");
            return TeleportTransition.missingRespawnBlock(player, postTeleportTransition);
        }

        ServerLevel level = server.getLevel(storedDim);
        if (level == null) {
            LOG.info("[BedBoat] level for dim {} is null — falling back", storedDim);
            return TeleportTransition.missingRespawnBlock(player, postTeleportTransition);
        }

        // Force-load the chunk at the stored position so entity lookups work.
        level.getChunk(storedPos.getX() >> 4, storedPos.getZ() >> 4);

        // Primary: search within 8 blocks of the stored position.
        BedBoatEntity boat = findBoatNear(level, storedPos);
        LOG.info("[BedBoat] AABB search near {} found: {}", storedPos, boat != null ? boat.getUUID() : "null");

        // Fallback: look up the specific entity by UUID (handles drift).
        Optional<UUID> uuidOpt = player.getData(Boats.BED_BOAT_UUID.get());
        LOG.info("[BedBoat] UUID attachment: {}", uuidOpt.map(UUID::toString).orElse("empty"));
        if (boat == null && uuidOpt.isPresent()) {
            Entity e = level.getEntity(uuidOpt.get());
            LOG.info("[BedBoat] entity by UUID: {}", e != null ? e.getClass().getSimpleName() + " " + e.getUUID() : "null");
            if (e instanceof BedBoatEntity b) boat = b;
        }

        if (boat != null) {
            Vec3 spawnPos = new Vec3(boat.getX(), boat.getY() + 0.6, boat.getZ());
            LOG.info("[BedBoat] SUCCESS (entity found) — spawning at {} on boat {}", spawnPos, boat.getUUID());
            return new TeleportTransition(
                level, spawnPos, Vec3.ZERO, boat.getYRot(), 0.0f,
                postTeleportTransition
            );
        }

        // Entity not found (boat in unloaded chunk or drifted far away).
        // If UUID is stored, the player explicitly set this bed boat as their spawn — use the stored position.
        if (uuidOpt.isPresent()) {
            Vec3 spawnPos = Vec3.atCenterOf(storedPos).add(0, 0.6, 0);
            LOG.info("[BedBoat] SUCCESS (stored pos fallback) — spawning at {}", spawnPos);
            return new TeleportTransition(level, spawnPos, Vec3.ZERO, 0.0f, 0.0f, postTeleportTransition);
        }

        LOG.info("[BedBoat] no UUID stored — falling back to missingRespawnBlock");
        return TeleportTransition.missingRespawnBlock(player, postTeleportTransition);
    }

    private static BedBoatEntity findBoatNear(ServerLevel level, BlockPos pos) {
        List<BedBoatEntity> boats = level.getEntitiesOfClass(
            BedBoatEntity.class,
            new AABB(pos).inflate(8.0)
        );
        if (boats.isEmpty()) return null;
        Vec3 center = Vec3.atCenterOf(pos);
        return boats.stream()
            .min(Comparator.comparingDouble(b -> b.position().distanceToSqr(center)))
            .orElse(null);
    }
}
