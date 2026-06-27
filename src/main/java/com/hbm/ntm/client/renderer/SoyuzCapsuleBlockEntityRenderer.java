package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SoyuzCapsuleBlockEntityRenderer implements BlockEntityRenderer<SoyuzCapsuleBlockEntity> {
    public SoyuzCapsuleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SoyuzCapsuleBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.25D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-25.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(15.0F));
        ObjSoyuzModels.renderLanderCapsule(false, poseStack, buffer, modelLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
