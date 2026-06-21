package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.RBMKAutoloaderBlockEntity;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class RBMKAutoloaderRenderer implements BlockEntityRenderer<RBMKAutoloaderBlockEntity> {
    public RBMKAutoloaderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RBMKAutoloaderBlockEntity autoloader, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = autoloader.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(autoloader, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);
        LegacyRbmkMachineRenderer.renderAutoloader(context,
                autoloader.lastPiston(), autoloader.renderPiston(), partialTick);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKAutoloaderBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }
}
