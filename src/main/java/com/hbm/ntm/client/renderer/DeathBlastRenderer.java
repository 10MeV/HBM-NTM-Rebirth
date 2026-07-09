package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.entity.logic.DeathBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class DeathBlastRenderer extends EntityRenderer<DeathBlastEntity> {
    private static final double BEAM_HEIGHT = 250.0D;
    private static final float DIAGONAL = 0.70710677F;
    private static final float[] BEAM_UNIT_X = {
            1.0F, DIAGONAL, 0.0F, -DIAGONAL, -1.0F, -DIAGONAL, 0.0F, DIAGONAL, 1.0F
    };
    private static final float[] BEAM_UNIT_Z = {
            0.0F, DIAGONAL, 1.0F, DIAGONAL, 0.0F, -DIAGONAL, -1.0F, -DIAGONAL, 0.0F
    };

    public DeathBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DeathBlastEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        VertexConsumer beamConsumer = buffer.getBuffer(LegacyUntexturedQuadRenderer.type(
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 180));
        PoseStack.Pose beamPose = poseStack.last();
        renderBeam(beamConsumer, beamPose, 0.5F, 255, 0, 0, 180);
        renderBeam(beamConsumer, beamPose, 0.25F, 255, 0, 255, 180);
        renderOrb(entity, partialTick, poseStack, buffer);
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderBeam(VertexConsumer consumer, PoseStack.Pose pose, float radius,
            int red, int green, int blue, int alpha) {
        int color = (red << 16) | (green << 8) | blue;
        for (int i = 0; i < 8; i++) {
            float x1 = BEAM_UNIT_X[i] * radius;
            float z1 = BEAM_UNIT_Z[i] * radius;
            float x2 = BEAM_UNIT_X[i + 1] * radius;
            float z2 = BEAM_UNIT_Z[i + 1] * radius;
            LegacyUntexturedQuadRenderer.quad(consumer, pose,
                    x1, BEAM_HEIGHT, z1,
                    x1, 0.0D, z1,
                    x2, 0.0D, z2,
                    x2, BEAM_HEIGHT, z2,
                    color, alpha, alpha, alpha, alpha);
        }
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
        Matrix4f position = poseStack.last().pose();
        VertexConsumer innerConsumer = ObjEffectModels.dynamicUntexturedConsumer(buffer, alpha, true);
        ObjEffectModels.renderSphereNewDynamicUntextured(innerConsumer, position, 255, 0, 255, alpha);
        poseStack.scale(1.25F, 1.25F, 1.25F);
        int outerAlpha = Math.max(1, alpha / 8);
        VertexConsumer outerConsumer = ObjEffectModels.dynamicUntexturedConsumer(buffer, outerAlpha, true);
        for (int i = 0; i < 8; i++) {
            ObjEffectModels.renderSphereNewDynamicUntextured(outerConsumer, position, 255, 0, 0, outerAlpha);
            poseStack.scale(1.05F, 1.05F, 1.05F);
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DeathBlastEntity entity) {
        return null;
    }
}
