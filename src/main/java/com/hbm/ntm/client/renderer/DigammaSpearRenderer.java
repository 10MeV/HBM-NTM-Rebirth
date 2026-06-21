package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.entity.effect.DigammaSpearEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

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
        ObjWeaponModels.LANCE.renderPart("Spear", ObjWeaponModels.LANCE_TEXTURE, poseStack, buffer,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        if (entity.ticksInGround > 0) {
            int alpha = Mth.clamp((int) (((entity.ticksInGround + partialTick) / 100.0F) * 255.0F), 0, 255);
            ObjWeaponModels.LANCE.renderPartAdditive("Spear", ObjWeaponModels.LANCE_TEXTURE, poseStack, buffer,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 255, 255, 255, alpha);
            renderFlash((entity.ticksInGround + partialTick) / 200.0D, poseStack, buffer);
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderFlash(double intensity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.scale(0.2F, 0.2F, 0.2F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();
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

                Matrix4f rotatedPose = poseStack.last().pose();
                float vert1 = (FLASH_RANDOM.nextFloat() * 20.0F + 15.0F) * scale;
                float vert2 = (FLASH_RANDOM.nextFloat() * 2.0F + 3.0F) * scale;
                int alpha = Mth.clamp((int) (intensity * intensity * 510.0D), 0, 255);
                putVertex(consumer, rotatedPose, 0.0F, 0.0F, 0.0F, alpha);
                putVertex(consumer, rotatedPose, -0.866F * vert2, vert1, -0.5F * vert2, 0);
                putVertex(consumer, rotatedPose, 0.866F * vert2, vert1, -0.5F * vert2, 0);
                putVertex(consumer, rotatedPose, 0.0F, vert1, vert2, 0);
                putVertex(consumer, rotatedPose, -0.866F * vert2, vert1, -0.5F * vert2, 0);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }

    private static void putVertex(VertexConsumer consumer, Matrix4f pose,
            float x, float y, float z, int alpha) {
        consumer.vertex(pose, x, y, z)
                .color(255, 153, 153, alpha)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DigammaSpearEntity entity) {
        return ObjWeaponModels.LANCE_TEXTURE;
    }
}
