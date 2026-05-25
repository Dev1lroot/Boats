/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats;

import com.dev1lroot.mcmods.boats.entity.BedBoatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = Boats.MODID)
public class BedBoatEvents {

    private static final Logger LOG = LoggerFactory.getLogger("boats/BedBoatEvents");

    @SubscribeEvent
    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        Optional<UUID> boatUUID = sp.getData(Boats.BED_BOAT_UUID.get());
        if (boatUUID.isEmpty()) return;

        BlockPos newSpawn = event.getNewSpawn();
        if (newSpawn == null) {
            // Spawn being reset — clear boat link so vanilla world spawn is used.
            sp.setData(Boats.BED_BOAT_UUID.get(), Optional.empty());
            LOG.info("[BedBoat] Spawn reset — cleared boat UUID for {}", sp.getName().getString());
            return;
        }

        // If the block at the new spawn position is a bed, this came from a land-based bed.
        // Clear the boat UUID so vanilla respawn logic takes over.
        ServerLevel level = sp.level().getServer().getLevel(event.getSpawnLevel());
        if (level == null) return;

        if (level.getBlockState(newSpawn).getBlock() instanceof BedBlock) {
            sp.setData(Boats.BED_BOAT_UUID.get(), Optional.empty());
            LOG.info("[BedBoat] Land bed used — cleared boat UUID for {}", sp.getName().getString());
        }
        // Otherwise (air/water at position) this is our boat updating spawn — keep UUID.
    }

    @SubscribeEvent
    public static void onPlayerRespawnPosition(PlayerRespawnPositionEvent event) {
        LOG.info("[BedBoat] PlayerRespawnPositionEvent fired, missingRespawnBlock={}",
            event.getTeleportTransition().missingRespawnBlock());

        // Only intervene when vanilla found no valid respawn block.
        if (!event.getTeleportTransition().missingRespawnBlock()) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();

        // UUID is stable even after the boat drifts far from the stored block position.
        Optional<UUID> boatUUID = player.getData(Boats.BED_BOAT_UUID.get());
        LOG.info("[BedBoat] Event: player={} boatUUID={}", player.getName().getString(),
            boatUUID.map(UUID::toString).orElse("empty"));
        if (boatUUID.isEmpty()) return;

        // Determine which level to search using the stored respawn config.
        ServerPlayer.RespawnConfig config = player.getRespawnConfig();
        ResourceKey<Level> storedDim = config != null ? config.respawnData().dimension() : Level.OVERWORLD;
        BlockPos storedPos = config != null ? config.respawnData().pos() : null;
        LOG.info("[BedBoat] Event: storedDim={} storedPos={}", storedDim, storedPos);

        ServerLevel level = player.level().getServer().getLevel(storedDim);
        if (level == null) { LOG.info("[BedBoat] Event: level null — abort"); return; }

        // Force-load the chunk at the stored position before searching.
        if (storedPos != null) {
            level.getChunk(storedPos.getX() >> 4, storedPos.getZ() >> 4);
        }

        Entity entity = level.getEntity(boatUUID.get());
        LOG.info("[BedBoat] Event: entity by UUID = {}", entity != null ? entity.getClass().getSimpleName() : "null");

        Vec3 spawnPos;
        float yaw;
        if (entity instanceof BedBoatEntity boat) {
            spawnPos = new Vec3(boat.getX(), boat.getY() + 0.6, boat.getZ());
            yaw = boat.getYRot();
            // Refresh stored position to boat's current location.
            player.setRespawnPosition(
                new ServerPlayer.RespawnConfig(
                    LevelData.RespawnData.of(level.dimension(), BlockPos.containing(boat.position()), yaw, 0f),
                    false
                ),
                false
            );
            LOG.info("[BedBoat] Event: SUCCESS (entity) — spawning at {}", spawnPos);
        } else if (storedPos != null) {
            // Boat in unloaded chunk or drifted; use stored position rather than world spawn.
            spawnPos = Vec3.atCenterOf(storedPos).add(0, 0.6, 0);
            yaw = 0.0f;
            LOG.info("[BedBoat] Event: SUCCESS (stored pos fallback) — spawning at {}", spawnPos);
        } else {
            LOG.info("[BedBoat] Event: no entity and no stored pos — abort");
            return;
        }

        TeleportTransition original = event.getTeleportTransition();
        event.setTeleportTransition(new TeleportTransition(
            level, spawnPos, Vec3.ZERO, yaw, 0.0f,
            original.postTeleportTransition()
        ));
        event.setCopyOriginalSpawnPosition(true);
    }
}
