package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.blockentity.FusionMHDTBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FusionMHDTRenderer implements BlockEntityRenderer<FusionMHDTBlockEntity> {
    public FusionMHDTRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionMHDTBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(FusionMHDTBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(FusionMHDTBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, FusionBreederRenderer.rotation(state));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjFusionModels.renderMhdtPart(ObjFusionModels.MHDT_LEGACY, ObjFusionModels.MHDT_TEXTURE,
                    poseStack, buffer, light, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Turbine");

            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                float rotor = blockEntity.getRotor(partialTick) % 15.0F;
                poseStack.translate(0.0D, 1.5D, 0.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, rotor);
                poseStack.translate(0.0D, -1.5D, 0.0D);
                ObjFusionModels.renderMhdtPart(ObjFusionModels.MHDT_LEGACY, ObjFusionModels.MHDT_TEXTURE,
                        poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL, "Coils");
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }
}
