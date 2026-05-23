/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.init;

import com.dev1lroot.mcmods.boats.Boats;
import com.dev1lroot.mcmods.boats.item.FunctionalBoatItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Boats.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Boats.MODID);

    public static final DeferredItem<FunctionalBoatItem> CRAFTING_TABLE_BOAT = ITEMS.registerItem(
        "crafting_table_boat",
        props -> new FunctionalBoatItem(ModEntities.CRAFTING_TABLE_BOAT, props.stacksTo(1))
    );

    public static final DeferredItem<FunctionalBoatItem> ENDER_CHEST_BOAT = ITEMS.registerItem(
        "ender_chest_boat",
        props -> new FunctionalBoatItem(ModEntities.ENDER_CHEST_BOAT, props.stacksTo(1))
    );

    public static final DeferredItem<FunctionalBoatItem> FURNACE_BOAT = ITEMS.registerItem(
        "furnace_boat",
        props -> new FunctionalBoatItem(ModEntities.FURNACE_BOAT, props.stacksTo(1))
    );

    public static final DeferredItem<FunctionalBoatItem> DOUBLE_CHEST_BOAT = ITEMS.registerItem(
        "double_chest_boat",
        props -> new FunctionalBoatItem(ModEntities.DOUBLE_CHEST_BOAT, props.stacksTo(1))
    );

    public static final DeferredItem<FunctionalBoatItem> BED_BOAT = ITEMS.registerItem(
        "bed_boat",
        props -> new FunctionalBoatItem(ModEntities.BED_BOAT, props.stacksTo(1))
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BOATS_TAB = CREATIVE_MODE_TABS.register(
        "boats_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.boats"))
            .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .icon(() -> CRAFTING_TABLE_BOAT.get().getDefaultInstance())
            .displayItems((params, output) -> {
                output.accept(CRAFTING_TABLE_BOAT.get());
                output.accept(ENDER_CHEST_BOAT.get());
                output.accept(FURNACE_BOAT.get());
                output.accept(DOUBLE_CHEST_BOAT.get());
                output.accept(BED_BOAT.get());
            })
            .build()
    );
}
