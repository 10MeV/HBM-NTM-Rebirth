package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.SoyuzCapsuleBlock;
import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

public class SoyuzCapsuleBlockEntityRenderer implements BlockEntityRenderer<SoyuzCapsuleBlockEntity> {
    public SoyuzCapsuleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(SoyuzCapsuleBlockEntity blockEntity, Vec3 cameraPos) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(SoyuzCapsuleBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.25D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -25.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 15.0F);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            ObjSoyuzModels.renderLanderCapsule(SoyuzCapsuleBlock.isRusted(blockEntity.getBlockState()),
                    poseStack, buffer, modelLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }
}
