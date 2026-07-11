package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.entity.effect.DigammaSpearEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

public class DigammaSpearRenderer extends EntityRenderer<DigammaSpearEntity> {
    private static final int FLASH_RAY_COUNT = 64;
    private static final int FLASH_COLOR = 0xFF9999;
    private static final float[] FLASH_LEFT_X = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_LEFT_Y = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_LEFT_Z = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_RIGHT_X = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_RIGHT_Y = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_RIGHT_Z = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_BACK_X = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_BACK_Y = new float[FLASH_RAY_COUNT];
    private static final float[] FLASH_BACK_Z = new float[FLASH_RAY_COUNT];

    static {
        precomputeFlashGeometry();
    }

    public DigammaSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(DigammaSpearEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 15.0D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
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

    private static void precomputeFlashGeometry() {
        Random random = new Random(432L);
        Quaternionf rotation = new Quaternionf();
        Vector3f point = new Vector3f();
        for (int i = 0; i < FLASH_RAY_COUNT; i++) {
            rotation.rotateX(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateY(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateZ(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateX(random.nextFloat() * ((float) Math.PI * 2.0F))
                    .rotateY(random.nextFloat() * ((float) Math.PI * 2.0F));
            float length = random.nextFloat() * 20.0F + 15.0F;
            float width = random.nextFloat() * 2.0F + 3.0F;
            storeFlashPoint(i, point.set(-0.866F * width, length, -0.5F * width).rotate(rotation),
                    FLASH_LEFT_X, FLASH_LEFT_Y, FLASH_LEFT_Z);
            storeFlashPoint(i, point.set(0.866F * width, length, -0.5F * width).rotate(rotation),
                    FLASH_RIGHT_X, FLASH_RIGHT_Y, FLASH_RIGHT_Z);
            storeFlashPoint(i, point.set(0.0F, length, width).rotate(rotation),
                    FLASH_BACK_X, FLASH_BACK_Y, FLASH_BACK_Z);
        }
    }

    private static void storeFlashPoint(int index, Vector3f point, float[] xs, float[] ys, float[] zs) {
        xs[index] = point.x();
        ys[index] = point.y();
        zs[index] = point.z();
    }

    private static void renderFlash(double intensity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.scale(0.2F, 0.2F, 0.2F);
        float scale = (float) (intensity * intensity * 25.0D);
        if (scale <= 0.0F) {
            poseStack.popPose();
            return;
        }

        int alpha = Mth.clamp((int) (intensity * intensity * 510.0D), 0, 255);
        VertexConsumer consumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0, VertexFormat.Mode.TRIANGLES));
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < FLASH_RAY_COUNT; i++) {
            float leftX = FLASH_LEFT_X[i] * scale;
            float leftY = FLASH_LEFT_Y[i] * scale;
            float leftZ = FLASH_LEFT_Z[i] * scale;
            float rightX = FLASH_RIGHT_X[i] * scale;
            float rightY = FLASH_RIGHT_Y[i] * scale;
            float rightZ = FLASH_RIGHT_Z[i] * scale;
            float backX = FLASH_BACK_X[i] * scale;
            float backY = FLASH_BACK_Y[i] * scale;
            float backZ = FLASH_BACK_Z[i] * scale;

            renderFlashTriangle(consumer, pose, alpha,
                    leftX, leftY, leftZ,
                    rightX, rightY, rightZ);
            renderFlashTriangle(consumer, pose, alpha,
                    rightX, rightY, rightZ,
                    backX, backY, backZ);
            renderFlashTriangle(consumer, pose, alpha,
                    backX, backY, backZ,
                    leftX, leftY, leftZ);
        }
        poseStack.popPose();
    }

    private static void renderFlashTriangle(VertexConsumer consumer, PoseStack.Pose pose, int alpha,
            float x1, float y1, float z1,
            float x2, float y2, float z2) {
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, 0.0D, 0.0D, 0.0D, FLASH_COLOR, alpha);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x1, y1, z1, FLASH_COLOR, 0);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x2, y2, z2, FLASH_COLOR, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(DigammaSpearEntity entity) {
        return ObjWeaponModels.LANCE_TEXTURE;
    }
}
