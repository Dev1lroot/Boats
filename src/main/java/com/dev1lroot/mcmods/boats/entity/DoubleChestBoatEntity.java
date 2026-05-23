/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.entity;

import com.dev1lroot.mcmods.boats.init.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class DoubleChestBoatEntity extends Boat implements Container, MenuProvider {

    private static final int CONTAINER_SIZE = 54;
    private static final Component DISPLAY_NAME = Component.translatable("container.boats.double_chest_boat");

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public DoubleChestBoatEntity(EntityType<? extends DoubleChestBoatEntity> type, Level level) {
        super(type, level, () -> ModItems.DOUBLE_CHEST_BOAT.get());
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitVec) {
        // Call super to let Entity.interact handle leashing.
        // On the server, AbstractBoat returns PASS (canAddPassenger=false prevents mounting).
        // On the client, AbstractBoat may return SUCCESS (it doesn't run canAddPassenger there),
        // but we ignore that false positive for non-ridable boats.
        InteractionResult superResult = super.interact(player, hand, hitVec);
        if (superResult != InteractionResult.PASS
                && !(level().isClientSide() && !player.isSecondaryUseActive())) {
            // A real leash interaction (non-PASS result that isn't the client-side riding guess).
            return superResult;
        }
        if (!level().isClientSide()) {
            player.openMenu(this);
        }
        return level().isClientSide() ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return ChestMenu.sixRows(containerId, inventory, this);
    }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return ContainerHelper.removeItem(items, slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !isRemoved() && player.distanceToSqr(this) < 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void setChanged() {}

    // --- Save / Load ---

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ContainerHelper.saveAllItems(output, items);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    // --- Drop contents on destroy ---

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!level().isClientSide() && reason.shouldDestroy()) {
            Containers.dropContents(level(), this, this);
        }
        super.remove(reason);
    }
}
