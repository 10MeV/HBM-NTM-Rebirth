package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.entity.logic.DeathBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class DeathBlastRenderer extends EntityRenderer<DeathBlastEntity> {
    private static final double BEAM_HEIGHT = 250.0D;

    public DeathBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DeathBlastEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        renderBeam(poseStack, buffer, 0.5F, 255, 0, 0, 180);
        renderBeam(poseStack, buffer, 0.25F, 255, 0, 255, 180);
        renderOrb(entity, partialTick, poseStack, buffer);
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderBeam(PoseStack poseStack, MultiBufferSource buffer, float radius,
            int red, int green, int blue, int alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();
        for (int i = 0; i < 8; i++) {
            double first = Math.toRadians(i * 45.0D);
            double second = Math.toRadians((i + 1) * 45.0D);
            float x1 = Mth.cos((float) first) * radius;
            float z1 = Mth.sin((float) first) * radius;
            float x2 = Mth.cos((float) second) * radius;
            float z2 = Mth.sin((float) second) * radius;
            putColorVertex(consumer, pose, x1, (float) BEAM_HEIGHT, z1, red, green, blue, alpha);
            putColorVertex(consumer, pose, x1, 0.0F, z1, red, green, blue, alpha);
            putColorVertex(consumer, pose, x2, 0.0F, z2, red, green, blue, alpha);
            putColorVertex(consumer, pose, x2, (float) BEAM_HEIGHT, z2, red, green, blue, alpha);
        }
    }

    private static void putColorVertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z,
            int red, int green, int blue, int alpha) {
        consumer.vertex(pose, x, y, z)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    private static void renderOrb(DeathBlastEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer) {
        double age = Math.min(entity.tickCount + partialTick, DeathBlastEntity.MAX_AGE);
        double progress = age / DeathBlastEntity.MAX_AGE;
        double scale = Math.max(10.0D - 10.0D * progress, 0.0D);
        int alpha = Mth.clamp((int) (progress * 255.0D), 0, 255);
        if (scale <= 0.0D || alpha <= 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale((float) scale, (float) scale, (float) scale);
        ObjEffectModels.SPHERE_NEW.renderAllUntextured(poseStack, buffer, 255, 0, 255, alpha, true);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        int outerAlpha = Math.max(1, alpha / 8);
        for (int i = 0; i < 8; i++) {
            ObjEffectModels.SPHERE_NEW.renderAllUntextured(poseStack, buffer, 255, 0, 0, outerAlpha, true);
            poseStack.scale(1.05F, 1.05F, 1.05F);
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DeathBlastEntity entity) {
        return null;
    }
}
