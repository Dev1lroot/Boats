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
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
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

import java.util.Map;

@Mod(value = Boats.MODID, dist = Dist.CLIENT)
public class BoatsClient {

    private static final Map<String, ModelLayerLocation> BOAT_LAYERS = Map.ofEntries(
        Map.entry("oak",      ModelLayers.OAK_BOAT),
        Map.entry("spruce",   ModelLayers.SPRUCE_BOAT),
        Map.entry("birch",    ModelLayers.BIRCH_BOAT),
        Map.entry("jungle",   ModelLayers.JUNGLE_BOAT),
        Map.entry("acacia",   ModelLayers.ACACIA_BOAT),
        Map.entry("dark_oak", ModelLayers.DARK_OAK_BOAT),
        Map.entry("mangrove", ModelLayers.MANGROVE_BOAT),
        Map.entry("cherry",   ModelLayers.CHERRY_BOAT),
        Map.entry("bamboo",   ModelLayers.BAMBOO_RAFT),
        Map.entry("pale_oak", ModelLayers.PALE_OAK_BOAT)
    );

    public BoatsClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onRegisterMenuScreens);
        modEventBus.addListener(this::onRegisterRenderers);
    }

    private void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.FURNACE_BOAT_MENU.get(), FurnaceBoatScreen::new);
    }

    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (String wood : ModEntities.WOOD_IDS) {
            ModelLayerLocation layer = BOAT_LAYERS.get(wood);
            registerBoat(event, ModEntities.CRAFTING_TABLE_BOATS.get(wood).get(), layer,
                new BlockEntry(Blocks.CRAFTING_TABLE.defaultBlockState(), 0f, 0f, 0.60f));
            registerBoat(event, ModEntities.ENDER_CHEST_BOATS.get(wood).get(), layer,
                new BlockEntry(Blocks.ENDER_CHEST.defaultBlockState(), 0f, 0f, 0.60f));
            registerBoat(event, ModEntities.FURNACE_BOATS.get(wood).get(), layer,
                new BlockEntry(Blocks.FURNACE.defaultBlockState(), 0f, 0f, 0.60f));
            registerBoat(event, ModEntities.DOUBLE_CHEST_BOATS.get(wood).get(), layer,
                new BlockEntry(Blocks.CHEST.defaultBlockState()
                    .setValue(ChestBlock.FACING, Direction.EAST)
                    .setValue(ChestBlock.TYPE, ChestType.LEFT), 0f, 0f, -0.5f),
                new BlockEntry(Blocks.CHEST.defaultBlockState()
                    .setValue(ChestBlock.FACING, Direction.EAST)
                    .setValue(ChestBlock.TYPE, ChestType.RIGHT), 0f, 0f, 0.5f));
            registerBoat(event, ModEntities.BED_BOATS.get(wood).get(), layer,
                new BlockEntry(Blocks.RED_BED.defaultBlockState()
                    .setValue(BedBlock.PART, BedPart.HEAD), 0f, 0f, -0.5f),
                new BlockEntry(Blocks.RED_BED.defaultBlockState()
                    .setValue(BedBlock.PART, BedPart.FOOT), 0f, 0f, 0.5f));
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerBoat(EntityRenderersEvent.RegisterRenderers event,
            EntityType<?> type, ModelLayerLocation layer, BlockEntry... entries) {
        event.registerEntityRenderer(
            (EntityType<AbstractBoat>) type,
            ctx -> new FunctionalBoatRenderer(ctx, layer, entries)
        );
    }
}
