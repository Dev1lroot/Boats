/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.block.state.BlockState;

public class FunctionalBoatRenderer extends BoatRenderer {

    public record BlockEntry(BlockState state, float dx, float dy, float dz) {}

    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;
    private final BlockEntry[] entries;

    public FunctionalBoatRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelId, BlockEntry... entries) {
        super(context, modelId);
        this.blockModelResolver = context.getBlockModelResolver();
        this.entries = entries;
    }

    @Override
    public FunctionalBoatRenderState createRenderState() {
        return new FunctionalBoatRenderState(entries.length);
    }

    @Override
    public void extractRenderState(AbstractBoat entity, BoatRenderState renderState, float partialTicks) {
        super.extractRenderState(entity, renderState, partialTicks);
        if (renderState instanceof FunctionalBoatRenderState state) {
            for (int i = 0; i < entries.length; i++) {
                blockModelResolver.update(state.displayBlocks[i], entries[i].state(), DISPLAY_CONTEXT);
            }
        }
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        super.submitTypeAdditions(renderState, poseStack, submitNodeCollector, lightCoords);
        if (!(renderState instanceof FunctionalBoatRenderState state)) return;

        for (int i = 0; i < entries.length; i++) {
            BlockModelRenderState block = state.displayBlocks[i];
            if (block.isEmpty()) continue;

            BlockEntry entry = entries[i];
            poseStack.pushPose();

            // AbstractBoatRenderer.submit() has already applied scale(-1,-1,1) and rotateY(90)
            // before calling submitTypeAdditions. That inverts both X and Y, making blocks render
            // upside-down and underground. Apply scale(-1,-1,1) to undo the flip: the combined
            // result S(-1,-1,1) × R_Y(90) × S(-1,-1,1) equals R_Y(-90), giving a normal
            // right-handed coordinate system (Y up) with a -90° residual rotation.
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.scale(0.8F, 0.8F, 0.8F);

            // Cancel the -90° residual so the block's default facing aligns with the boat bow.
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

            // Place the block centred horizontally on the boat hull.
            // Y = -0.1875 aligns the block's bottom with the interior floor of the hull.
            // Per-entry dx/dy/dz offsets allow shifting blocks along the boat axes.
            poseStack.translate(-0.5F + entry.dx(), -0.1875F + entry.dy(), -0.5F + entry.dz());

            block.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, renderState.outlineColor);
            poseStack.popPose();
        }
    }
}
