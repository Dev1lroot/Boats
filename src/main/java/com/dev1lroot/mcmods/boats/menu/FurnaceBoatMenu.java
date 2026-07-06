/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.menu;

import com.dev1lroot.mcmods.boats.init.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipePropertySet;

public class FurnaceBoatMenu extends AbstractFurnaceMenu {

    public FurnaceBoatMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.FURNACE_BOAT_MENU.get(), RecipePropertySet.FURNACE_INPUT,
            RecipeBookType.FURNACE, containerId, inventory);
    }

    public FurnaceBoatMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenuTypes.FURNACE_BOAT_MENU.get(), RecipePropertySet.FURNACE_INPUT,
            RecipeBookType.FURNACE, containerId, inventory, container, data);
    }
}
