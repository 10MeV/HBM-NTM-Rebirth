package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.world.level.block.state.BlockState;

public record ObjRenderContext(
        PoseStack poseStack,
        MultiBufferSource buffer,
        BlockState state,
        int packedLight,
        int packedOverlay
) {
    public BlockRenderDispatcher blockRenderer() {
        return Minecraft.getInstance().getBlockRenderer();
    }

    public ModelBlockRenderer modelRenderer() {
        return blockRenderer().getModelRenderer();
    }
}
