package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class LegacyAssemblySparkRenderer {
    public static final double WIDE = 0.1875D;
    public static final double NARROW = 0.0D;
    public static final double LENGTH = 1.25D;
    public static final double EPSILON = 0.01D;
    public static final double MIRRORED_U_OFFSET = 0.5D;

    public static void renderPlan(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTileRenderPlans.AssemblySparkRenderPlan plan) {
        if (!plan.active()) {
            return;
        }
        LegacyTexturedRenderMode renderMode = plan.blend() == null
                ? LegacyTexturedRenderMode.CUTOUT_NO_CULL
                : plan.blend().modernRenderMode();
        int resolvedLight = plan.fullbright() == null
                ? packedLight
                : LegacyTexturedQuadRenderer.legacyLightmap(plan.fullbright().lightmapX(),
                        plan.fullbright().lightmapY());
        VertexConsumer consumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(texture, buffer, renderMode);
        for (LegacyTileRenderPlans.AssemblySparkBladePlan blade : plan.blades()) {
            poseStack.pushPose();
            poseStack.translate(blade.translateX(), blade.translateY(), blade.translateZ());
            PoseStack.Pose pose = poseStack.last();
            for (LegacyTileRenderPlans.TexturedQuadPlan quad : blade.quads()) {
                LegacyTileRenderPlans.QuadVertexPlan v0 = quad.vertices().get(0);
                LegacyTileRenderPlans.QuadVertexPlan v1 = quad.vertices().get(1);
                LegacyTileRenderPlans.QuadVertexPlan v2 = quad.vertices().get(2);
                LegacyTileRenderPlans.QuadVertexPlan v3 = quad.vertices().get(3);
                LegacyTexturedQuadRenderer.quadWithVertexColors(consumer, pose, resolvedLight, packedOverlay,
                        0.0F, 1.0F, 0.0F,
                        v0.x(), v0.y(), v0.z(), v0.u(), v0.v(), v0.color(), v0.alpha(),
                        v1.x(), v1.y(), v1.z(), v1.u(), v1.v(), v1.color(), v1.alpha(),
                        v2.x(), v2.y(), v2.z(), v2.u(), v2.v(), v2.color(), v2.alpha(),
                        v3.x(), v3.y(), v3.z(), v3.u(), v3.v(), v3.color(), v3.alpha());
            }
            poseStack.popPose();
        }
    }

    public static void renderDirect(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, long worldTime, float partialTick,
            double slide1, double slide2, double arm2BladeAngle, double arm2StrikerOffset,
            double arm4BladeAngle, double arm4StrikerOffset) {
        boolean renderBlade2 = arm2StrikerOffset <= LegacyTileRenderPlans.ASSEMBLY_STRIKER_SPARK_THRESHOLD;
        boolean renderBlade4 = arm4StrikerOffset <= LegacyTileRenderPlans.ASSEMBLY_STRIKER_SPARK_THRESHOLD;
        if (!renderBlade2 && !renderBlade4) {
            return;
        }
        double uMin = (worldTime / 10.0D + partialTick) % 10.0D;
        double uMax = uMin + 1.0D;
        VertexConsumer sparkConsumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(texture, buffer,
                LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE);
        PoseStack.Pose pose = poseStack.last();
        if (renderBlade2) {
            renderSparkBlade(sparkConsumer, pose, LightTexture.FULL_BRIGHT, packedOverlay,
                    0.5D + slide1, 1.0625D, -arm2BladeAngle / 45.0D,
                    LegacyTileRenderPlans.ASSEMBLY_SPARK_LENGTH, uMin, uMax);
        }
        if (renderBlade4) {
            renderSparkBlade(sparkConsumer, pose, LightTexture.FULL_BRIGHT, packedOverlay,
                    -0.5D - slide2, 1.0625D, arm4BladeAngle / 45.0D,
                    -LegacyTileRenderPlans.ASSEMBLY_SPARK_LENGTH, uMin, uMax);
        }
    }

    private static void renderSparkBlade(VertexConsumer consumer, PoseStack.Pose pose,
            int packedLight, int packedOverlay, double offsetX, double offsetY, double offsetZ,
            double length, double uMin, double uMax) {
        renderSparkQuad(consumer, pose, packedLight, packedOverlay,
                offsetX - EPSILON, offsetY, offsetZ, length,
                uMin + MIRRORED_U_OFFSET, uMax + MIRRORED_U_OFFSET, 0.0D, 1.0D, 1.0D, 0.0D);
        renderSparkQuad(consumer, pose, packedLight, packedOverlay,
                offsetX + EPSILON, offsetY, offsetZ, length,
                uMin, uMax, 1.0D, 0.0D, 0.0D, 1.0D);
    }

    private static void renderSparkQuad(VertexConsumer consumer, PoseStack.Pose pose,
            int packedLight, int packedOverlay, double x, double y, double z, double length, double uMin, double uMax,
            double vLengthNeg, double vLengthPos, double vTipPos, double vTipNeg) {
        LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, packedLight, packedOverlay,
                0.0F, 1.0F, 0.0F,
                x, y - WIDE, z + length, uMin, vLengthNeg, 0,
                x, y + WIDE, z + length, uMin, vLengthPos, 0,
                x, y + NARROW, z, uMax, vTipPos, 255,
                x, y - NARROW, z, uMax, vTipNeg, 255,
                0xFFFFFF);
    }

    private LegacyAssemblySparkRenderer() {
    }
}
