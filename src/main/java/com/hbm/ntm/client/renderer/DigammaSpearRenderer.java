package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.entity.effect.DigammaSpearEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Random;

public class DigammaSpearRenderer extends EntityRenderer<DigammaSpearEntity> {
    private static final Random FLASH_RANDOM = new Random(432L);

    public DigammaSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(DigammaSpearEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 15.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        ObjWeaponModels.renderLanceSpear(poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        if (entity.ticksInGround > 0) {
            int alpha = Mth.clamp((int) (((entity.ticksInGround + partialTick) / 100.0F) * 255.0F), 0, 255);
            ObjWeaponModels.renderLanceSpearAdditive(poseStack, buffer,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 255, 255, 255, alpha);
            renderFlash((entity.ticksInGround + partialTick) / 200.0D, poseStack, buffer);
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderFlash(double intensity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.scale(0.2F, 0.2F, 0.2F);
        float scale = (float) (intensity * intensity * 25.0D);
        if (scale <= 0.0F) {
            poseStack.popPose();
            return;
        }

        synchronized (FLASH_RANDOM) {
            FLASH_RANDOM.setSeed(432L);
            for (int i = 0; i < 64; i++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(FLASH_RANDOM.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(FLASH_RANDOM.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(FLASH_RANDOM.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(FLASH_RANDOM.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(FLASH_RANDOM.nextFloat() * 360.0F));

                float vert1 = (FLASH_RANDOM.nextFloat() * 20.0F + 15.0F) * scale;
                float vert2 = (FLASH_RANDOM.nextFloat() * 2.0F + 3.0F) * scale;
                int alpha = Mth.clamp((int) (intensity * intensity * 510.0D), 0, 255);
                renderFlashTriangle(poseStack, buffer, alpha,
                        0.0F, 0.0F, 0.0F,
                        -0.866F * vert2, vert1, -0.5F * vert2,
                        0.866F * vert2, vert1, -0.5F * vert2);
                renderFlashTriangle(poseStack, buffer, alpha,
                        0.0F, 0.0F, 0.0F,
                        0.866F * vert2, vert1, -0.5F * vert2,
                        0.0F, vert1, vert2);
                renderFlashTriangle(poseStack, buffer, alpha,
                        0.0F, 0.0F, 0.0F,
                        0.0F, vert1, vert2,
                        -0.866F * vert2, vert1, -0.5F * vert2);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    private static void renderFlashTriangle(PoseStack poseStack, MultiBufferSource buffer, int alpha,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2) {
        LegacyWavefrontModel.renderUntexturedVertexColorTransientTriangle(poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                x0, y0, z0, 0xFF9999, alpha,
                x1, y1, z1, 0xFF9999, 0,
                x2, y2, z2, 0xFF9999, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(DigammaSpearEntity entity) {
        return ObjWeaponModels.LANCE_TEXTURE;
    }
}
