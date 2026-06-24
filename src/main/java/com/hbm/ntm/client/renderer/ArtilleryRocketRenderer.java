package com.hbm.ntm.client.renderer;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.client.obj.ObjTurretModels;
import com.hbm.ntm.entity.projectile.ArtilleryRocketEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ArtilleryRocketRenderer extends EntityRenderer<ArtilleryRocketEntity> {
    public ArtilleryRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(ArtilleryRocketEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        LegacyArtilleryAmmoCatalog.HimarsRocket rocket = entity.ammoType();
        ResourceLocation texture = himarsRocketTexture(rocket);
        String part = rocket.modelType() == 1 ? "RocketSingle" : "RocketStandard";

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) - 90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        ObjTurretModels.renderPart(ObjTurretModels.HIMARS, part, texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static ResourceLocation himarsRocketTexture(LegacyArtilleryAmmoCatalog.HimarsRocket rocket) {
        return switch (rocket.legacyName()) {
            case "ammo_himars_standard_he" -> ObjProjectileModels.HIMARS_STANDARD_HE_TEXTURE;
            case "ammo_himars_standard_wp" -> ObjProjectileModels.HIMARS_STANDARD_WP_TEXTURE;
            case "ammo_himars_standard_tb" -> ObjProjectileModels.HIMARS_STANDARD_TB_TEXTURE;
            case "ammo_himars_standard_lava" -> ObjProjectileModels.HIMARS_STANDARD_LAVA_TEXTURE;
            case "ammo_himars_standard_mini_nuke" -> ObjProjectileModels.HIMARS_STANDARD_MINI_NUKE_TEXTURE;
            case "ammo_himars_single" -> ObjProjectileModels.HIMARS_SINGLE_TEXTURE;
            case "ammo_himars_single_tb" -> ObjProjectileModels.HIMARS_SINGLE_TB_TEXTURE;
            default -> ObjProjectileModels.HIMARS_STANDARD_TEXTURE;
        };
    }

    @Override
    public ResourceLocation getTextureLocation(ArtilleryRocketEntity entity) {
        return himarsRocketTexture(entity.ammoType());
    }
}
