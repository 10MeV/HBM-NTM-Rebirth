package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class LegacyUntexturedQuadRenderer {
    public static VertexConsumer lightning(MultiBufferSource buffer) {
        return buffer.getBuffer(RenderType.lightning());
    }

    public static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
                              int red, int green, int blue, int alpha) {
        consumer.vertex(pose.pose(), (float) x, (float) y, (float) z)
                .color(red, green, blue, alpha)
                .uv2(LightTexture.FULL_BRIGHT)
                .endVertex();
    }

    public static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                            double x0, double y0, double z0,
                            double x1, double y1, double z1,
                            double x2, double y2, double z2,
                            double x3, double y3, double z3,
                            int red, int green, int blue,
                            int alpha0, int alpha1, int alpha2, int alpha3) {
        vertex(consumer, pose, x0, y0, z0, red, green, blue, alpha0);
        vertex(consumer, pose, x1, y1, z1, red, green, blue, alpha1);
        vertex(consumer, pose, x2, y2, z2, red, green, blue, alpha2);
        vertex(consumer, pose, x3, y3, z3, red, green, blue, alpha3);
    }

    public static void doubleSidedQuad(VertexConsumer consumer, PoseStack.Pose pose,
                                       double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       double x2, double y2, double z2,
                                       double x3, double y3, double z3,
                                       int red, int green, int blue,
                                       int alpha0, int alpha1, int alpha2, int alpha3) {
        quad(consumer, pose, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, red, green, blue, alpha0, alpha1, alpha2, alpha3);
        quad(consumer, pose, x3, y3, z3, x2, y2, z2, x1, y1, z1, x0, y0, z0, red, green, blue, alpha3, alpha2, alpha1, alpha0);
    }

    private LegacyUntexturedQuadRenderer() {
    }
}
