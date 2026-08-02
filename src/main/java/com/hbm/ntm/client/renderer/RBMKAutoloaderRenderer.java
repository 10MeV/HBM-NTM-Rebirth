package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.RBMKAutoloaderBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class RBMKAutoloaderRenderer implements BlockEntityRenderer<RBMKAutoloaderBlockEntity> {
    // TileEntityRBMKAutoloader#getMaxRenderDistanceSquared() returned 65536,
    // which maps to a 256-block modern renderer distance.
    private static final int LEGACY_VIEW_DISTANCE = 256;

    public RBMKAutoloaderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RBMKAutoloaderBlockEntity autoloader, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(autoloader, getViewDistance())) {
            return;
        }
        int light = LegacyRenderLighting.resolveMultiblockLight(autoloader, packedLight);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(autoloader);
                var samplingScope = LegacyRenderLighting.pushModelViewSampling(autoloader, poseStack.last().pose())) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                LegacyRbmkMachineRenderer.renderAutoloaderBase(poseStack, buffer, light, packedOverlay,
                        LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            }
            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(autoloader)) {
                LegacyRbmkMachineRenderer.renderAutoloaderPiston(poseStack, buffer, light, packedOverlay,
                        autoloader.lastPiston(), autoloader.renderPiston(), partialTick,
                        LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            }
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKAutoloaderBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(RBMKAutoloaderBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LEGACY_VIEW_DISTANCE;
    }
}
