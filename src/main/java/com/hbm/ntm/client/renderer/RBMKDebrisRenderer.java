package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.entity.projectile.RBMKDebrisEntity;
import com.hbm.ntm.neutron.RBMKDebrisPlanner.RBMKDebrisType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RBMKDebrisRenderer extends EntityRenderer<RBMKDebrisEntity> {
    private static final Vector3f LEGACY_ROTATION_AXIS = new Vector3f(1.0F, 1.0F, 1.0F).normalize();

    public RBMKDebrisRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.2F;
    }

    @Override
    public void render(RBMKDebrisEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.125D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getId() % 360));
        float rotation = entity.debrisRotationO + (entity.debrisRotation - entity.debrisRotationO) * partialTick;
        poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(rotation), LEGACY_ROTATION_AXIS));
        model(entity.getDebrisType()).renderAll(texture(entity.getDebrisType()), poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RBMKDebrisEntity entity) {
        return texture(entity.getDebrisType());
    }

    private static LegacyWavefrontModel model(RBMKDebrisType type) {
        return switch (type) {
            case BLANK -> ObjProjectileModels.DEBRIS_BLANK;
            case ELEMENT -> ObjProjectileModels.DEBRIS_ELEMENT;
            case FUEL -> ObjProjectileModels.DEBRIS_FUEL;
            case GRAPHITE -> ObjProjectileModels.DEBRIS_GRAPHITE;
            case LID -> ObjProjectileModels.DEBRIS_LID;
            case ROD -> ObjProjectileModels.DEBRIS_ROD;
        };
    }

    private static ResourceLocation texture(RBMKDebrisType type) {
        return switch (type) {
            case BLANK -> ObjProjectileModels.RBMK_DEBRIS_BLANK_TEXTURE;
            case ELEMENT -> ObjProjectileModels.RBMK_DEBRIS_SIDE_TEXTURE;
            case FUEL -> ObjProjectileModels.RBMK_DEBRIS_FUEL_TEXTURE;
            case GRAPHITE -> ObjProjectileModels.GRAPHITE_TEXTURE;
            case LID -> ObjProjectileModels.RBMK_DEBRIS_LID_TEXTURE;
            case ROD -> ObjProjectileModels.RBMK_DEBRIS_CONTROL_TEXTURE;
        };
    }
}
