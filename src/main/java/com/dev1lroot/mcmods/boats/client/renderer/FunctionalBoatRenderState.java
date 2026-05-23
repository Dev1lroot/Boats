/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.dev1lroot.mcmods.boats.client.renderer;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.BoatRenderState;

public class FunctionalBoatRenderState extends BoatRenderState {
    public final BlockModelRenderState[] displayBlocks;
    public boolean isLit;

    public FunctionalBoatRenderState(int count) {
        this.displayBlocks = new BlockModelRenderState[count];
        for (int i = 0; i < count; i++) {
            this.displayBlocks[i] = new BlockModelRenderState();
        }
    }
}
