/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class CraftingTableBoatEntity extends Boat implements HasCustomInventoryScreen {

    private static final Component DISPLAY_NAME = Component.translatable("container.crafting");

    public CraftingTableBoatEntity(EntityType<? extends CraftingTableBoatEntity> type, Level level, Supplier<Item> boatItem) {
        super(type, level, boatItem);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitVec) {
        // Let leashing and other priority interactions (Entity.interact) pass through first,
        // then let AbstractBoat handle mounting when not sneaking.
        InteractionResult superResult = super.interact(player, hand, hitVec);
        if (superResult != InteractionResult.PASS) {
            return superResult;
        }
        // Mirror AbstractChestBoat: pass through if the player could still mount (not sneaking).
        if (canAddPassenger(player) && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide()) {
            openGui(player);
        }
        return level().isClientSide() ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    // Called server-side when the player presses E while riding.
    @Override
    public void openCustomInventoryScreen(Player player) {
        openGui(player);
    }

    private void openGui(Player player) {
        // ContainerLevelAccess.NULL always passes the stillValid check (returns default=true),
        // so the menu stays open while the boat moves — unlike create(level, pos) which
        // checks for a real crafting table block at the position.
        player.openMenu(new SimpleMenuProvider(
            (id, inv, p) -> new CraftingMenu(id, inv, ContainerLevelAccess.NULL),
            DISPLAY_NAME
        ));
    }
}
