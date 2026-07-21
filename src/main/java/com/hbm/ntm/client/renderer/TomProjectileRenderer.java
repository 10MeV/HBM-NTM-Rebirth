package com.hbm.ntm.client.renderer;

import com.hbm.ntm.entity.projectile.TomProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Modern renderer for the descending TOM model, preserving RenderTom's y-50 origin. */
public final class TomProjectileRenderer extends EntityRenderer<TomProjectileEntity> {
    public TomProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(TomProjectileEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -50.0D, 0.0D);
        LegacyTomRenderer.renderTom(poseStack, buffer, OverlayTexture.NO_OVERLAY, System.currentTimeMillis());
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TomProjectileEntity entity) {
        return null;
    }
}
