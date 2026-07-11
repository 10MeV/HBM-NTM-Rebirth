package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.entity.missile.AntiBallisticMissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AntiBallisticMissileRenderer extends EntityRenderer<AntiBallisticMissileEntity> {
    public AntiBallisticMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5F;
    }

    @Override
    public void render(AntiBallisticMissileEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()));
        LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F - Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));
        ObjMissilePartModels.MISSILE_ABM.renderAll(ObjMissilePartModels.MISSILE_ABM_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AntiBallisticMissileEntity entity) {
        return ObjMissilePartModels.MISSILE_ABM_TEXTURE;
    }
}
