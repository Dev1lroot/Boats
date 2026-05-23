/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats;

import com.dev1lroot.mcmods.boats.client.renderer.FunctionalBoatRenderer;
import com.dev1lroot.mcmods.boats.client.renderer.FunctionalBoatRenderer.BlockEntry;
import com.dev1lroot.mcmods.boats.client.screen.FurnaceBoatScreen;
import com.dev1lroot.mcmods.boats.init.ModEntities;
import com.dev1lroot.mcmods.boats.init.ModMenuTypes;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Boats.MODID, dist = Dist.CLIENT)
public class BoatsClient {

    public BoatsClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onRegisterMenuScreens);
        modEventBus.addListener(this::onRegisterRenderers);
    }

    private void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.FURNACE_BOAT_MENU.get(), FurnaceBoatScreen::new);
    }

    @SuppressWarnings("unchecked")
    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Single-block boats: block shifted slightly toward stern (Z+) so it sits more centred in the hull
        event.registerEntityRenderer(
            (net.minecraft.world.entity.EntityType<AbstractBoat>)(net.minecraft.world.entity.EntityType<?>)
                ModEntities.CRAFTING_TABLE_BOAT.get(),
            ctx -> new FunctionalBoatRenderer(ctx, ModelLayers.OAK_BOAT,
                new BlockEntry(Blocks.CRAFTING_TABLE.defaultBlockState(), 0f, 0f, 0.60f))
        );
        event.registerEntityRenderer(
            (net.minecraft.world.entity.EntityType<AbstractBoat>)(net.minecraft.world.entity.EntityType<?>)
                ModEntities.ENDER_CHEST_BOAT.get(),
            ctx -> new FunctionalBoatRenderer(ctx, ModelLayers.OAK_BOAT,
                new BlockEntry(Blocks.ENDER_CHEST.defaultBlockState(), 0f, 0f, 0.60f))
        );
        event.registerEntityRenderer(
            (net.minecraft.world.entity.EntityType<AbstractBoat>)(net.minecraft.world.entity.EntityType<?>)
                ModEntities.FURNACE_BOAT.get(),
            ctx -> new FunctionalBoatRenderer(ctx, ModelLayers.OAK_BOAT,
                new BlockEntry(Blocks.FURNACE.defaultBlockState(), 0f, 0f, 0.60f))
        );
        // Double chest: two half-chest models (TYPE=RIGHT at bow, TYPE=LEFT at stern) form a connected double chest.
        // FACING=EAST so the chest opens to the side and the halves align along the boat's Z axis.
        event.registerEntityRenderer(
            (net.minecraft.world.entity.EntityType<AbstractBoat>)(net.minecraft.world.entity.EntityType<?>)
                ModEntities.DOUBLE_CHEST_BOAT.get(),
            ctx -> new FunctionalBoatRenderer(ctx, ModelLayers.OAK_BOAT,
                new BlockEntry(Blocks.CHEST.defaultBlockState()
                    .setValue(ChestBlock.FACING, Direction.EAST)
                    .setValue(ChestBlock.TYPE, ChestType.LEFT), 0f, 0f, -0.5f),
                new BlockEntry(Blocks.CHEST.defaultBlockState()
                    .setValue(ChestBlock.FACING, Direction.EAST)
                    .setValue(ChestBlock.TYPE, ChestType.RIGHT), 0f, 0f,  0.5f))
        );
        // Bed: HEAD at bow (Z-), FOOT at stern (Z+); FACING=NORTH so both parts share orientation.
        event.registerEntityRenderer(
            (net.minecraft.world.entity.EntityType<AbstractBoat>)(net.minecraft.world.entity.EntityType<?>)
                ModEntities.BED_BOAT.get(),
            ctx -> new FunctionalBoatRenderer(ctx, ModelLayers.OAK_BOAT,
                new BlockEntry(Blocks.RED_BED.defaultBlockState()
                    .setValue(BedBlock.PART, BedPart.HEAD), 0f, 0f, -0.5f),
                new BlockEntry(Blocks.RED_BED.defaultBlockState()
                    .setValue(BedBlock.PART, BedPart.FOOT), 0f, 0f,  0.5f))
        );
    }
}
