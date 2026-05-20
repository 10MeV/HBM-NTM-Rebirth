package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.world.level.block.state.BlockState;

public record ObjRenderContext(
        PoseStack poseStack,
        MultiBufferSource buffer,
        BlockState state,
        int packedLight,
        int packedOverlay,
        int color,
        boolean hasColor,
        boolean legacyShadow
) {
    public ObjRenderContext(PoseStack poseStack, MultiBufferSource buffer, BlockState state, int packedLight, int packedOverlay) {
        this(poseStack, buffer, state, packedLight, packedOverlay, 0xFFFFFF, false, false);
    }

    public BlockRenderDispatcher blockRenderer() {
        return Minecraft.getInstance().getBlockRenderer();
    }

    public ModelBlockRenderer modelRenderer() {
        return blockRenderer().getModelRenderer();
    }

    public ObjRenderContext withColor(int color) {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color & 0xFFFFFF, true, legacyShadow);
    }

    public ObjRenderContext clearColor() {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, 0xFFFFFF, false, legacyShadow);
    }

    public ObjRenderContext fullBright() {
        return new ObjRenderContext(poseStack, buffer, state, LightTexture.FULL_BRIGHT, packedOverlay, color, hasColor, legacyShadow);
    }

    public ObjRenderContext withLegacyShadow() {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color, hasColor, true);
    }

    public ObjRenderContext withoutLegacyShadow() {
        return new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay, color, hasColor, false);
    }
}
