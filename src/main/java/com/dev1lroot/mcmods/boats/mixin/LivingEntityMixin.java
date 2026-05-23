/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.mixin;

import com.dev1lroot.mcmods.boats.entity.BedBoatEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * LivingEntity.tick() calls checkBedExists() each tick and stops sleeping
     * if the block at sleepingPos is not a BedBlock. Since the bed boat floats
     * on water (not a bed block), this always interrupts sleep. Redirect the
     * call so that riding a BedBoatEntity counts as a valid sleeping surface.
     */
    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/world/entity/LivingEntity;checkBedExists()Z")
    )
    private boolean boats$redirectCheckBedExists(LivingEntity entity) {
        if (entity.getVehicle() instanceof BedBoatEntity) {
            return true;
        }
        // Replicate the original check: bed block must exist at the sleeping pos.
        return entity.getSleepingPos()
            .map(pos -> entity.level().getBlockState(pos).getBlock() instanceof BedBlock)
            .orElse(false);
    }
}
