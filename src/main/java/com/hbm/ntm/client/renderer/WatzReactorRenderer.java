package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.WatzReactorBlockEntity;
import com.hbm.ntm.client.obj.ObjReactorModels;
import com.hbm.ntm.client.render.shader.HbmShaderCompatibilityDetector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class WatzReactorRenderer implements BlockEntityRenderer<WatzReactorBlockEntity> {
    public WatzReactorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(WatzReactorBlockEntity blockEntity) {
        return HbmShaderCompatibilityDetector.shouldRenderBlockEntityOffScreen();
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(WatzReactorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(blockEntity);
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        try (LegacyRenderLighting.ModelViewSamplingScope ignored =
                LegacyRenderLighting.pushModelViewSampling(blockEntity, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            ObjReactorModels.WATZ.renderAll(ObjReactorModels.WATZ_TEXTURE, poseStack, buffer, light, packedOverlay);
            poseStack.popPose();
        }
    }
}
