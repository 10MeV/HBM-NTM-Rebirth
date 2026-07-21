package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.entity.effect.LegacyVentCloudEntity;
import com.hbm.entity.particle.EntityOrangeFX;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

/** Source-shaped replacement for 1.7.10 MultiCloudRenderer. */
public class VentCloudRenderer extends EntityRenderer<LegacyVentCloudEntity> {
    public VentCloudRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public void render(LegacyVentCloudEntity cloud, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        int age = Math.max(0, cloud.tickCount);
        int maxAge = Math.max(1, cloud.getMaxAge());
        int frame = Math.min(7, age * 8 / maxAge);
        TextureAtlasSprite sprite = LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID,
                "item/" + prefix(cloud) + (frame + 1));
        Random shades = new Random(cloud.hashCode());
        Random offsets = new Random(100L);
        poseStack.pushPose();
        poseStack.scale(3.75F, 3.75F, 3.75F);
        for (int i = 0; i < 5; i++) {
            double x = (offsets.nextGaussian() - 1.0D) * 0.15D;
            double y = (offsets.nextGaussian() - 1.0D) * 0.15D;
            double z = (offsets.nextGaussian() - 1.0D) * 0.15D;
            float size = (float) (offsets.nextDouble() * 0.5D + 0.25D);
            int shade = 255 - shades.nextInt(10) * 13;
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.scale(size, size, size);
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            LegacyTexturedQuadRenderer.spriteQuad(sprite, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                    LegacyTexturedRenderMode.TRANSLUCENT, 0.0F, 1.0F, 0.0F,
                    LegacyTexturedQuadRenderer.spriteUnitVertex(-0.5D, -0.25D, 0.0D, 0.0F, 1.0F,
                            LegacyTexturedQuadRenderer.rgb(shade, shade, shade), 255),
                    LegacyTexturedQuadRenderer.spriteUnitVertex(0.5D, -0.25D, 0.0D, 1.0F, 1.0F,
                            LegacyTexturedQuadRenderer.rgb(shade, shade, shade), 255),
                    LegacyTexturedQuadRenderer.spriteUnitVertex(0.5D, 0.75D, 0.0D, 1.0F, 0.0F,
                            LegacyTexturedQuadRenderer.rgb(shade, shade, shade), 255),
                    LegacyTexturedQuadRenderer.spriteUnitVertex(-0.5D, 0.75D, 0.0D, 0.0F, 0.0F,
                            LegacyTexturedQuadRenderer.rgb(shade, shade, shade), 255));
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(cloud, yaw, partialTick, poseStack, buffer, packedLight);
    }
    private static String prefix(LegacyVentCloudEntity cloud) { return cloud instanceof EntityOrangeFX ? "orange" : cloud.isChlorine() ? "chlorine" : cloud.isPink() ? "pc" : "cloud"; }
    @Override public ResourceLocation getTextureLocation(LegacyVentCloudEntity cloud) { return null; }
}
