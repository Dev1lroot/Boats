/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.entity;

import com.dev1lroot.mcmods.boats.init.ModItems;
import com.dev1lroot.mcmods.boats.menu.FurnaceBoatMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FurnaceBoatEntity extends Boat implements Container, MenuProvider, HasCustomInventoryScreen {

    private static final int CONTAINER_SIZE = 3;
    private static final Component DISPLAY_NAME = Component.translatable("container.boats.furnace_boat");

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    private int litTimeRemaining;
    private int litTotalTime;
    private int cookingTimer;
    private int cookingTotalTime = 200;

    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck =
        RecipeManager.createCheck(RecipeType.SMELTING);

    private final ContainerData furnaceData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> litTotalTime > 0
                    ? Mth.floor(((double) litTimeRemaining / litTotalTime) * Short.MAX_VALUE)
                    : 0;
                case 1 -> litTotalTime > 0 ? Short.MAX_VALUE : 0;
                case 2 -> cookingTimer;
                case 3 -> cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> litTimeRemaining = litTotalTime > 0
                    ? Mth.floor(((double) value / Short.MAX_VALUE) * litTotalTime)
                    : 0;
                case 1 -> litTotalTime = value;
                case 2 -> cookingTimer = value;
                case 3 -> cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public FurnaceBoatEntity(EntityType<? extends FurnaceBoatEntity> type, Level level) {
        super(type, level, () -> ModItems.FURNACE_BOAT.get());
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
            player.openMenu(this);
        }
        return level().isClientSide() ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
        }
    }

    private void serverTick(ServerLevel level) {
        boolean wasLit = litTimeRemaining > 0;

        if (litTimeRemaining > 0) {
            litTimeRemaining--;
        }

        ItemStack fuel = items.get(1);
        ItemStack ingredient = items.get(0);
        boolean hasIngredient = !ingredient.isEmpty();
        boolean hasFuel = !fuel.isEmpty();

        if (hasIngredient) {
            SingleRecipeInput input = new SingleRecipeInput(ingredient);
            RecipeHolder<? extends AbstractCookingRecipe> recipe = quickCheck.getRecipeFor(input, level).orElse(null);

            if (recipe != null) {
                ItemStack result = recipe.value().assemble(input);
                if (!result.isEmpty() && canBurn(result)) {
                    if (litTimeRemaining == 0 && hasFuel) {
                        int newLitTime = fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues());
                        if (newLitTime > 0) {
                            consumeFuel(fuel);
                            litTimeRemaining = newLitTime;
                            litTotalTime = newLitTime;
                        }
                    }

                    if (litTimeRemaining > 0) {
                        cookingTotalTime = recipe.value().cookingTime();
                        cookingTimer++;
                        if (cookingTimer >= cookingTotalTime) {
                            cookingTimer = 0;
                            burnItem(ingredient, result);
                        }
                    } else {
                        cookingTimer = 0;
                    }
                } else {
                    cookingTimer = 0;
                }
            }
        } else {
            cookingTimer = Mth.clamp(cookingTimer - 2, 0, cookingTotalTime);
        }
    }

    private boolean canBurn(ItemStack result) {
        ItemStack output = items.get(2);
        if (output.isEmpty()) return true;
        if (!output.getItem().equals(result.getItem())) return false;
        int newCount = output.getCount() + result.getCount();
        return newCount <= getMaxStackSize() && newCount <= output.getMaxStackSize();
    }

    private void burnItem(ItemStack ingredient, ItemStack result) {
        ItemStack output = items.get(2);
        if (output.isEmpty()) {
            items.set(2, result.copy());
        } else {
            output.grow(result.getCount());
        }
        ingredient.shrink(1);
    }

    private void consumeFuel(ItemStack fuel) {
        ItemStackTemplate remainder = fuel.getCraftingRemainder();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            items.set(1, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FurnaceBoatMenu(containerId, inventory, this, furnaceData);
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
        output.putInt("LitTime", litTimeRemaining);
        output.putInt("LitTotalTime", litTotalTime);
        output.putInt("CookTime", cookingTimer);
        output.putInt("CookTimeTotal", cookingTotalTime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        litTimeRemaining = input.getIntOr("LitTime", 0);
        litTotalTime = input.getIntOr("LitTotalTime", 0);
        cookingTimer = input.getIntOr("CookTime", 0);
        cookingTotalTime = input.getIntOr("CookTimeTotal", 200);
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
