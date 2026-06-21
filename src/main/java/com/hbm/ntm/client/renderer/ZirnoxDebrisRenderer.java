package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.entity.projectile.ZirnoxDebrisEntity;
import com.hbm.ntm.neutron.RBMKDebrisPlanner.ZirnoxDebrisType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ZirnoxDebrisRenderer extends EntityRenderer<ZirnoxDebrisEntity> {
    private static final Vector3f LEGACY_ROTATION_AXIS = new Vector3f(1.0F, 1.0F, 1.0F).normalize();

    public ZirnoxDebrisRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.2F;
    }

    @Override
    public void render(ZirnoxDebrisEntity entity, float yaw, float partialTick, PoseStack poseStack,
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
    public ResourceLocation getTextureLocation(ZirnoxDebrisEntity entity) {
        return texture(entity.getDebrisType());
    }

    private static LegacyWavefrontModel model(ZirnoxDebrisType type) {
        return switch (type) {
            case BLANK -> ObjProjectileModels.ZIRNOX_DEBRIS_BLANK;
            case ELEMENT -> ObjProjectileModels.ZIRNOX_DEBRIS_ELEMENT;
            case SHRAPNEL -> ObjProjectileModels.ZIRNOX_DEBRIS_SHRAPNEL;
            case GRAPHITE -> ObjProjectileModels.DEBRIS_GRAPHITE;
            case CONCRETE -> ObjProjectileModels.ZIRNOX_DEBRIS_CONCRETE;
            case EXCHANGER -> ObjProjectileModels.ZIRNOX_DEBRIS_EXCHANGER;
        };
    }

    private static ResourceLocation texture(ZirnoxDebrisType type) {
        return switch (type) {
            case BLANK, SHRAPNEL, EXCHANGER -> ObjProjectileModels.ZIRNOX_TEXTURE;
            case ELEMENT -> ObjProjectileModels.ZIRNOX_DEBRIS_ELEMENT_TEXTURE;
            case GRAPHITE -> ObjProjectileModels.GRAPHITE_TEXTURE;
            case CONCRETE -> ObjProjectileModels.ZIRNOX_DESTROYED_TEXTURE;
        };
    }
}
