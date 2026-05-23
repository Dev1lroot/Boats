/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.client.renderer;

import com.dev1lroot.mcmods.boats.entity.FurnaceBoatEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FurnaceBoatRenderer extends FunctionalBoatRenderer {

    private static final BlockState FURNACE_OFF = Blocks.FURNACE.defaultBlockState();
    private static final BlockState FURNACE_ON = Blocks.FURNACE.defaultBlockState().setValue(BlockStateProperties.LIT, true);

    public FurnaceBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelId) {
        super(context, modelId, new BlockEntry(FURNACE_OFF, 0f, 0f, 0.60f));
    }

    @Override
    public void extractRenderState(AbstractBoat entity, BoatRenderState renderState, float partialTicks) {
        super.extractRenderState(entity, renderState, partialTicks);
        if (renderState instanceof FunctionalBoatRenderState state && entity instanceof FurnaceBoatEntity furnace) {
            state.isLit = furnace.isLit();
            blockModelResolver.update(state.displayBlocks[0], state.isLit ? FURNACE_ON : FURNACE_OFF, DISPLAY_CONTEXT);
        }
    }
}
