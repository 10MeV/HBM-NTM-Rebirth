package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.entity.projectile.ArtilleryShellEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ArtilleryShellRenderer extends EntityRenderer<ArtilleryShellEntity> {
    private static final LegacyWavefrontModel.SelectionHandle GRENADE =
            ObjProjectileModels.PROJECTILES.prepareRenderOnlyInCallOrder("Grenade");

    public ArtilleryShellRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(ArtilleryShellEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) - 90.0F));
        poseStack.scale(2.5F, 5.0F, 2.5F);
        ObjProjectileModels.PROJECTILES.renderOnlyInCallOrder(ObjProjectileModels.GRENADE_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, GRENADE);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ArtilleryShellEntity entity) {
        return ObjProjectileModels.GRENADE_TEXTURE;
    }
}
