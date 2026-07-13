package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.projectile.AirstrikeBombletEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AirstrikeBombletRenderer extends EntityRenderer<AirstrikeBombletEntity> {
    public AirstrikeBombletRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(AirstrikeBombletEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjProjectileModels.BOMBLET_ZETA.renderAll(ObjProjectileModels.BOMBLET_ZETA_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AirstrikeBombletEntity entity) {
        return ObjProjectileModels.BOMBLET_ZETA_TEXTURE;
    }
}
