package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.blockentity.FusionCouplerBlockEntity;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FusionCouplerRenderer implements BlockEntityRenderer<FusionCouplerBlockEntity> {
    public FusionCouplerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionCouplerBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(FusionCouplerBlockEntity blockEntity, Vec3 cameraPos) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(FusionCouplerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, FusionBreederRenderer.rotation(state));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjFusionModels.COUPLER_LEGACY.renderAll(ObjFusionModels.COUPLER_TEXTURE,
                    poseStack, buffer, light, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        }
        poseStack.popPose();
    }
}
