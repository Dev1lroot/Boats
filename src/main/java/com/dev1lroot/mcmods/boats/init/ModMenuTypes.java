/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.init;

import com.dev1lroot.mcmods.boats.Boats;
import com.dev1lroot.mcmods.boats.menu.FurnaceBoatMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(Registries.MENU, Boats.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FurnaceBoatMenu>> FURNACE_BOAT_MENU =
        MENU_TYPES.register("furnace_boat",
            () -> new MenuType<>(FurnaceBoatMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
