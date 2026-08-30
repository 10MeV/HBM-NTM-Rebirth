package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.blockentity.FusionKlystronCreativeBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjFusionModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FusionKlystronCreativeRenderer implements BlockEntityRenderer<FusionKlystronCreativeBlockEntity> {
    public FusionKlystronCreativeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(FusionKlystronCreativeBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(FusionKlystronCreativeBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(FusionKlystronCreativeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, FusionBreederRenderer.rotation(state));
        poseStack.translate(-1.0D, 0.0D, 0.0D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjFusionModels.renderKlystronPart(ObjFusionModels.KLYSTRON_LEGACY,
                    ObjFusionModels.KLYSTRON_CREATIVE_TEXTURE, poseStack, buffer, light, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_CULL, "Klystron");

            try (var animatedFadeScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(blockEntity)) {
                poseStack.pushPose();
                poseStack.translate(0.0D, 2.5D, 0.0D);
                LegacyPoseRotations.rotateXDegrees(poseStack, blockEntity.getFan(partialTick));
                poseStack.translate(0.0D, -2.5D, 0.0D);
                ObjFusionModels.renderKlystronPart(ObjFusionModels.KLYSTRON_LEGACY,
                        ObjFusionModels.KLYSTRON_CREATIVE_TEXTURE, poseStack, buffer, packedLight, packedOverlay,
                        LegacyTexturedRenderMode.CUTOUT_CULL, "Rotor");
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }
}
