package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class LegacyLineRenderer {
    public static final float DEFAULT_LINE_WIDTH = 1.0F;

    public static RenderType type(float lineWidth, LegacyTexturedRenderMode renderMode, int alpha) {
        return RenderType.lines();
    }

    public static VertexConsumer consumer(MultiBufferSource buffer, float lineWidth,
            LegacyTexturedRenderMode renderMode, int alpha) {
        return buffer.getBuffer(type(lineWidth, renderMode, alpha));
    }

    public static VertexConsumer consumer(ObjRenderContext context) {
        return consumer(context, DEFAULT_LINE_WIDTH);
    }

    public static VertexConsumer consumer(ObjRenderContext context, float lineWidth) {
        return consumer(context.buffer(), lineWidth, context.renderMode(), context.alpha());
    }

    public static void line(ObjRenderContext context,
            double x0, double y0, double z0, double x1, double y1, double z1) {
        line(context, DEFAULT_LINE_WIDTH, x0, y0, z0, x1, y1, z1, 0xFFFFFF, 255);
    }

    public static void line(ObjRenderContext context, float lineWidth,
            double x0, double y0, double z0, double x1, double y1, double z1, int color, int alpha) {
        line(consumer(context.buffer(), lineWidth, context.renderMode(), multipliedAlpha(context, alpha)),
                context.poseStack().last(), x0, y0, z0, x1, y1, z1,
                multipliedColor(context, color), multipliedAlpha(context, alpha));
    }

    public static void line(VertexConsumer consumer, PoseStack.Pose pose,
            double x0, double y0, double z0, double x1, double y1, double z1, int color, int alpha) {
        float normalX = (float) (x1 - x0);
        float normalY = (float) (y1 - y0);
        float normalZ = (float) (z1 - z0);
        float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        if (length <= 1.0E-6F) {
            normalY = 1.0F;
        } else {
            normalX /= length;
            normalY /= length;
            normalZ /= length;
        }
        vertex(consumer, pose, x0, y0, z0, color, alpha, normalX, normalY, normalZ);
        vertex(consumer, pose, x1, y1, z1, color, alpha, normalX, normalY, normalZ);
    }

    public static void linePositionColor(VertexConsumer consumer, PoseStack.Pose pose,
            double x0, double y0, double z0, double x1, double y1, double z1, int color, int alpha) {
        vertexPositionColor(consumer, pose, x0, y0, z0, color, alpha);
        vertexPositionColor(consumer, pose, x1, y1, z1, color, alpha);
    }

    public static void box(ObjRenderContext context,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        box(context, DEFAULT_LINE_WIDTH, minX, minY, minZ, maxX, maxY, maxZ, 0xFFFFFF, 255);
    }

    public static void box(ObjRenderContext context, float lineWidth,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, int alpha) {
        VertexConsumer consumer = consumer(context.buffer(), lineWidth, context.renderMode(), multipliedAlpha(context, alpha));
        PoseStack.Pose pose = context.poseStack().last();
        int multipliedColor = multipliedColor(context, color);
        int multipliedAlpha = multipliedAlpha(context, alpha);

        line(consumer, pose, minX, minY, minZ, minX, minY, maxZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, minX, minY, maxZ, maxX, minY, maxZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, maxX, minY, maxZ, maxX, minY, minZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, maxX, minY, minZ, minX, minY, minZ, multipliedColor, multipliedAlpha);

        line(consumer, pose, minX, maxY, minZ, minX, maxY, maxZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, maxX, maxY, maxZ, maxX, maxY, minZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, maxX, maxY, minZ, minX, maxY, minZ, multipliedColor, multipliedAlpha);

        line(consumer, pose, minX, minY, minZ, minX, maxY, minZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, maxX, minY, minZ, maxX, maxY, minZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, multipliedColor, multipliedAlpha);
        line(consumer, pose, minX, minY, maxZ, minX, maxY, maxZ, multipliedColor, multipliedAlpha);
    }

    public static void structurePreviewBox(ObjRenderContext context, double sizeX, double sizeY, double sizeZ) {
        structurePreviewBox(context, DEFAULT_LINE_WIDTH, sizeX, sizeY, sizeZ, 0xFFFFFF, 255);
    }

    public static void structurePreviewBox(ObjRenderContext context, float lineWidth,
            double sizeX, double sizeY, double sizeZ, int color, int alpha) {
        box(context, lineWidth, 0.0D, 1.0D, 0.0D, sizeX, sizeY + 1.0D, sizeZ, color, alpha);
    }

    public static void boxPositionColor(VertexConsumer consumer, PoseStack.Pose pose,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, int alpha) {
        linePositionColor(consumer, pose, minX, minY, minZ, maxX, minY, minZ, color, alpha);
        linePositionColor(consumer, pose, maxX, minY, minZ, maxX, minY, maxZ, color, alpha);
        linePositionColor(consumer, pose, maxX, minY, maxZ, minX, minY, maxZ, color, alpha);
        linePositionColor(consumer, pose, minX, minY, maxZ, minX, minY, minZ, color, alpha);

        linePositionColor(consumer, pose, minX, maxY, minZ, maxX, maxY, minZ, color, alpha);
        linePositionColor(consumer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, color, alpha);
        linePositionColor(consumer, pose, maxX, maxY, maxZ, minX, maxY, maxZ, color, alpha);
        linePositionColor(consumer, pose, minX, maxY, maxZ, minX, maxY, minZ, color, alpha);

        linePositionColor(consumer, pose, minX, minY, minZ, minX, maxY, minZ, color, alpha);
        linePositionColor(consumer, pose, maxX, minY, minZ, maxX, maxY, minZ, color, alpha);
        linePositionColor(consumer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, color, alpha);
        linePositionColor(consumer, pose, minX, minY, maxZ, minX, maxY, maxZ, color, alpha);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
            int color, int alpha, float normalX, float normalY, float normalZ) {
        consumer.vertex(pose.pose(), (float) x, (float) y, (float) z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, clampAlpha(alpha))
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    private static void vertexPositionColor(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z,
            int color, int alpha) {
        consumer.vertex(pose.pose(), (float) x, (float) y, (float) z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, clampAlpha(alpha))
                .endVertex();
    }

    private static int multipliedColor(ObjRenderContext context, int color) {
        if (!context.hasColor()) {
            return color & 0xFFFFFF;
        }
        int contextColor = context.color();
        int red = (contextColor >> 16 & 255) * (color >> 16 & 255) / 255;
        int green = (contextColor >> 8 & 255) * (color >> 8 & 255) / 255;
        int blue = (contextColor & 255) * (color & 255) / 255;
        return red << 16 | green << 8 | blue;
    }

    private static int multipliedAlpha(ObjRenderContext context, int alpha) {
        return clampAlpha(context.alpha() * clampAlpha(alpha) / 255);
    }

    private static int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private LegacyLineRenderer() {
    }
}
