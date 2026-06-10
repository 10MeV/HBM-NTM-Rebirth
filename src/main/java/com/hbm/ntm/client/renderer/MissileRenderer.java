package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.entity.missile.MissileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MissileRenderer extends EntityRenderer<MissileEntity> {
    public MissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.75F;
    }

    @Override
    public void render(MissileEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        model(entity).renderAll(texture(entity), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MissileEntity entity) {
        return texture(entity);
    }

    private static LegacyWavefrontModel model(MissileEntity entity) {
        return switch (entity.variant().formFactor()) {
            case STRONG -> ObjMissilePartModels.MISSILE_STRONG;
            case HUGE -> ObjMissilePartModels.MISSILE_HUGE;
            case ATLAS -> ObjMissilePartModels.MISSILE_ATLAS;
            case MICRO -> ObjMissilePartModels.MISSILE_MICRO;
            case ABM -> ObjMissilePartModels.MISSILE_ABM;
            case V2, OTHER -> ObjMissilePartModels.MISSILE_V2;
        };
    }

    private static ResourceLocation texture(MissileEntity entity) {
        return switch (entity.variant()) {
            case STRONG -> ObjMissilePartModels.MISSILE_STRONG_HE_TEXTURE;
            case INCENDIARY_STRONG -> ObjMissilePartModels.MISSILE_STRONG_IN_TEXTURE;
            case CLUSTER_STRONG -> ObjMissilePartModels.MISSILE_STRONG_CL_TEXTURE;
            case BUSTER_STRONG -> ObjMissilePartModels.MISSILE_STRONG_BU_TEXTURE;
            case BURST -> ObjMissilePartModels.MISSILE_HUGE_HE_TEXTURE;
            case DECOY -> ObjMissilePartModels.MISSILE_V2_DECOY_TEXTURE;
            case INCENDIARY -> ObjMissilePartModels.MISSILE_V2_IN_TEXTURE;
            case CLUSTER -> ObjMissilePartModels.MISSILE_V2_CL_TEXTURE;
            case BUSTER -> ObjMissilePartModels.MISSILE_V2_BU_TEXTURE;
            case GENERIC -> ObjMissilePartModels.MISSILE_V2_HE_TEXTURE;
        };
    }
}
