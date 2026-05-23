/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.init;

import com.dev1lroot.mcmods.boats.Boats;
import com.dev1lroot.mcmods.boats.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModEntities {

    public static final String[] WOOD_IDS = {
        "oak", "spruce", "birch", "jungle", "acacia",
        "dark_oak", "mangrove", "cherry", "bamboo", "pale_oak"
    };

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, Boats.MODID);

    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<CraftingTableBoatEntity>>> CRAFTING_TABLE_BOATS = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<EnderChestBoatEntity>>>   ENDER_CHEST_BOATS   = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<FurnaceBoatEntity>>>      FURNACE_BOATS        = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<DoubleChestBoatEntity>>>  DOUBLE_CHEST_BOATS   = new LinkedHashMap<>();
    public static final Map<String, DeferredHolder<EntityType<?>, EntityType<BedBoatEntity>>>          BED_BOATS            = new LinkedHashMap<>();

    static {
        for (String wood : WOOD_IDS) {
            register(wood);
        }
    }

    private static void register(String wood) {
        CRAFTING_TABLE_BOATS.put(wood, entity(wood + "_crafting_table_boat",
            (type, level) -> new CraftingTableBoatEntity(type, level,
                () -> ModItems.CRAFTING_TABLE_BOAT_ITEMS.get(wood).get())));
        ENDER_CHEST_BOATS.put(wood, entity(wood + "_ender_chest_boat",
            (type, level) -> new EnderChestBoatEntity(type, level,
                () -> ModItems.ENDER_CHEST_BOAT_ITEMS.get(wood).get())));
        FURNACE_BOATS.put(wood, entity(wood + "_furnace_boat",
            (type, level) -> new FurnaceBoatEntity(type, level,
                () -> ModItems.FURNACE_BOAT_ITEMS.get(wood).get())));
        DOUBLE_CHEST_BOATS.put(wood, entity(wood + "_double_chest_boat",
            (type, level) -> new DoubleChestBoatEntity(type, level,
                () -> ModItems.DOUBLE_CHEST_BOAT_ITEMS.get(wood).get())));
        BED_BOATS.put(wood, entity(wood + "_bed_boat",
            (type, level) -> new BedBoatEntity(type, level,
                () -> ModItems.BED_BOAT_ITEMS.get(wood).get())));
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractBoat> DeferredHolder<EntityType<?>, EntityType<T>> entity(
            String id, EntityType.EntityFactory<T> factory) {
        return (DeferredHolder<EntityType<?>, EntityType<T>>) (Object) ENTITY_TYPES.register(id, () ->
            EntityType.Builder.of(factory, MobCategory.MISC)
                .sized(1.375f, 0.5625f)
                .eyeHeight(0.5625f)
                .build(ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Boats.MODID, id))));
    }
}
