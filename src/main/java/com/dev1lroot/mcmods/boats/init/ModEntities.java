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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, Boats.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CraftingTableBoatEntity>> CRAFTING_TABLE_BOAT =
        ENTITY_TYPES.register("crafting_table_boat", () ->
            EntityType.Builder.<CraftingTableBoatEntity>of(CraftingTableBoatEntity::new, MobCategory.MISC)
                .sized(1.375f, 0.5625f)
                .eyeHeight(0.5625f)
                .build(ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Boats.MODID, "crafting_table_boat"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderChestBoatEntity>> ENDER_CHEST_BOAT =
        ENTITY_TYPES.register("ender_chest_boat", () ->
            EntityType.Builder.<EnderChestBoatEntity>of(EnderChestBoatEntity::new, MobCategory.MISC)
                .sized(1.375f, 0.5625f)
                .eyeHeight(0.5625f)
                .build(ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Boats.MODID, "ender_chest_boat"))));

    public static final DeferredHolder<EntityType<?>, EntityType<FurnaceBoatEntity>> FURNACE_BOAT =
        ENTITY_TYPES.register("furnace_boat", () ->
            EntityType.Builder.<FurnaceBoatEntity>of(FurnaceBoatEntity::new, MobCategory.MISC)
                .sized(1.375f, 0.5625f)
                .eyeHeight(0.5625f)
                .build(ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Boats.MODID, "furnace_boat"))));

    public static final DeferredHolder<EntityType<?>, EntityType<DoubleChestBoatEntity>> DOUBLE_CHEST_BOAT =
        ENTITY_TYPES.register("double_chest_boat", () ->
            EntityType.Builder.<DoubleChestBoatEntity>of(DoubleChestBoatEntity::new, MobCategory.MISC)
                .sized(1.375f, 0.5625f)
                .eyeHeight(0.5625f)
                .build(ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Boats.MODID, "double_chest_boat"))));

    public static final DeferredHolder<EntityType<?>, EntityType<BedBoatEntity>> BED_BOAT =
        ENTITY_TYPES.register("bed_boat", () ->
            EntityType.Builder.<BedBoatEntity>of(BedBoatEntity::new, MobCategory.MISC)
                .sized(1.375f, 0.5625f)
                .eyeHeight(0.5625f)
                .build(ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(Boats.MODID, "bed_boat"))));
}
