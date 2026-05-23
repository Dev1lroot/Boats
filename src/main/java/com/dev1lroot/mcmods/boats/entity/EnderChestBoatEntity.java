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
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class EnderChestBoatEntity extends Boat implements HasCustomInventoryScreen {

    private static final Component DISPLAY_NAME = Component.translatable("container.enderchest");

    public EnderChestBoatEntity(EntityType<? extends EnderChestBoatEntity> type, Level level, Supplier<Item> boatItem) {
        super(type, level, boatItem);
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15f;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitVec) {
        InteractionResult superResult = super.interact(player, hand, hitVec);
        if (superResult != InteractionResult.PASS) {
            return superResult;
        }
        if (canAddPassenger(player) && !player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide()) {
            openGui(player);
        }
        return level().isClientSide() ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        openGui(player);
    }

    private void openGui(Player player) {
        PlayerEnderChestContainer enderInv = player.getEnderChestInventory();
        player.openMenu(new SimpleMenuProvider(
            (id, inv, p) -> ChestMenu.threeRows(id, inv, enderInv),
            DISPLAY_NAME
        ));
    }
}
