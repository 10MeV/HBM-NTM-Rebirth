package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjEntityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.projectile.BoxcarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Exact legacy RenderBoxcar entity transform. */
public class BoxcarRenderer extends EntityRenderer<BoxcarEntity> {
    public BoxcarRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(BoxcarEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -1.5D);
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
        ObjEntityModels.BOXCAR.renderAll(ObjEntityModels.BOXCAR_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BoxcarEntity entity) {
        return ObjEntityModels.BOXCAR_TEXTURE;
    }
}
