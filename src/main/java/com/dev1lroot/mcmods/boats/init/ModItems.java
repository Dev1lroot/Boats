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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Boats.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Boats.MODID);

    public static final Map<String, DeferredItem<FunctionalBoatItem>> CRAFTING_TABLE_BOAT_ITEMS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<FunctionalBoatItem>> ENDER_CHEST_BOAT_ITEMS    = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<FunctionalBoatItem>> FURNACE_BOAT_ITEMS        = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<FunctionalBoatItem>> DOUBLE_CHEST_BOAT_ITEMS   = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<FunctionalBoatItem>> BED_BOAT_ITEMS            = new LinkedHashMap<>();

    static {
        for (String wood : ModEntities.WOOD_IDS) {
            var ct = ModEntities.CRAFTING_TABLE_BOATS.get(wood);
            var ec = ModEntities.ENDER_CHEST_BOATS.get(wood);
            var fn = ModEntities.FURNACE_BOATS.get(wood);
            var dc = ModEntities.DOUBLE_CHEST_BOATS.get(wood);
            var bd = ModEntities.BED_BOATS.get(wood);
            CRAFTING_TABLE_BOAT_ITEMS.put(wood, ITEMS.registerItem(wood + "_crafting_table_boat",
                props -> new FunctionalBoatItem(ct, props.stacksTo(1))));
            ENDER_CHEST_BOAT_ITEMS.put(wood, ITEMS.registerItem(wood + "_ender_chest_boat",
                props -> new FunctionalBoatItem(ec, props.stacksTo(1))));
            FURNACE_BOAT_ITEMS.put(wood, ITEMS.registerItem(wood + "_furnace_boat",
                props -> new FunctionalBoatItem(fn, props.stacksTo(1))));
            DOUBLE_CHEST_BOAT_ITEMS.put(wood, ITEMS.registerItem(wood + "_double_chest_boat",
                props -> new FunctionalBoatItem(dc, props.stacksTo(1))));
            BED_BOAT_ITEMS.put(wood, ITEMS.registerItem(wood + "_bed_boat",
                props -> new FunctionalBoatItem(bd, props.stacksTo(1))));
        }
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BOATS_TAB = CREATIVE_MODE_TABS.register(
        "boats_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.boats"))
            .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .icon(() -> CRAFTING_TABLE_BOAT_ITEMS.get("oak").get().getDefaultInstance())
            .displayItems((params, output) -> {
                for (String wood : ModEntities.WOOD_IDS) {
                    output.accept(CRAFTING_TABLE_BOAT_ITEMS.get(wood).get());
                    output.accept(ENDER_CHEST_BOAT_ITEMS.get(wood).get());
                    output.accept(FURNACE_BOAT_ITEMS.get(wood).get());
                    output.accept(DOUBLE_CHEST_BOAT_ITEMS.get(wood).get());
                    output.accept(BED_BOAT_ITEMS.get(wood).get());
                }
            })
            .build()
    );
}
