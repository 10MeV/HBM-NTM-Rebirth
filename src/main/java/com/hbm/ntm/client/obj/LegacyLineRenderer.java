package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.List;

public final class LegacyLineRenderer {
    public static final float DEFAULT_LINE_WIDTH = 1.0F;

    public static RenderType type(float lineWidth, LegacyTexturedRenderMode renderMode, int alpha) {
        return RenderType.lines();
    }

    public static VertexConsumer consumer(MultiBufferSource buffer, float lineWidth,
            LegacyTexturedRenderMode renderMode, int alpha) {
        return buffer.getBuffer(type(lineWidth, renderMode, alpha));
    }

    public static void line(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            float lineWidth, double x0, double y0, double z0, double x1, double y1, double z1,
            int color, int alpha) {
        int clampedAlpha = clampAlpha(alpha);
        LegacyWavefrontModel.renderUntexturedLineTransientLines(poseStack, buffer, renderMode, lineWidth,
                List.of(new LegacyWavefrontModel.UntexturedLineTransient(
                        x0, y0, z0, x1, y1, z1, color & 0xFFFFFF, clampedAlpha)));
    }

    public static void lines(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            float lineWidth, List<LegacyWavefrontModel.UntexturedLineTransient> lines) {
        LegacyWavefrontModel.renderUntexturedLineTransientLines(poseStack, buffer, renderMode, lineWidth, lines);
    }

    public static void box(PoseStack poseStack, MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
            float lineWidth, double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            int color, int alpha) {
        lines(poseStack, buffer, renderMode, lineWidth,
                boxLines(minX, minY, minZ, maxX, maxY, maxZ, color, alpha));
    }

    public static List<LegacyWavefrontModel.UntexturedLineTransient> boxLines(double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ, int color, int alpha) {
        List<LegacyWavefrontModel.UntexturedLineTransient> lines = new ArrayList<>(12);
        addBoxLine(lines, minX, minY, minZ, maxX, minY, minZ, color, alpha);
        addBoxLine(lines, maxX, minY, minZ, maxX, minY, maxZ, color, alpha);
        addBoxLine(lines, maxX, minY, maxZ, minX, minY, maxZ, color, alpha);
        addBoxLine(lines, minX, minY, maxZ, minX, minY, minZ, color, alpha);

        addBoxLine(lines, minX, maxY, minZ, maxX, maxY, minZ, color, alpha);
        addBoxLine(lines, maxX, maxY, minZ, maxX, maxY, maxZ, color, alpha);
        addBoxLine(lines, maxX, maxY, maxZ, minX, maxY, maxZ, color, alpha);
        addBoxLine(lines, minX, maxY, maxZ, minX, maxY, minZ, color, alpha);

        addBoxLine(lines, minX, minY, minZ, minX, maxY, minZ, color, alpha);
        addBoxLine(lines, maxX, minY, minZ, maxX, maxY, minZ, color, alpha);
        addBoxLine(lines, maxX, minY, maxZ, maxX, maxY, maxZ, color, alpha);
        addBoxLine(lines, minX, minY, maxZ, minX, maxY, maxZ, color, alpha);
        return lines;
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

    public static void linePositionColorIdentity(VertexConsumer consumer,
            double x0, double y0, double z0, double x1, double y1, double z1, int color, int alpha) {
        vertexPositionColorIdentity(consumer, x0, y0, z0, color, alpha);
        vertexPositionColorIdentity(consumer, x1, y1, z1, color, alpha);
    }

    public static void pointPositionColorIdentity(VertexConsumer consumer,
            double x, double y, double z, int color, int alpha) {
        vertexPositionColorIdentity(consumer, x, y, z, color, alpha);
    }

    public static void drawPositionColorLines(Tesselator tesselator,
            List<LegacyWavefrontModel.UntexturedLineTransient> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        emitPositionColorLines(builder, lines);
        tesselator.end();
    }

    public static void drawPositionColorLines(Tesselator tesselator, PoseStack.Pose pose,
            List<LegacyWavefrontModel.UntexturedLineTransient> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        emitPositionColorLines(builder, pose, lines);
        tesselator.end();
    }

    public static void emitPositionColorLines(VertexConsumer consumer,
            List<LegacyWavefrontModel.UntexturedLineTransient> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (LegacyWavefrontModel.UntexturedLineTransient line : lines) {
            linePositionColorIdentity(consumer, line.x0(), line.y0(), line.z0(), line.x1(), line.y1(), line.z1(),
                    line.color(), line.alpha());
        }
    }

    public static void emitPositionColorLines(VertexConsumer consumer, PoseStack.Pose pose,
            List<LegacyWavefrontModel.UntexturedLineTransient> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        for (LegacyWavefrontModel.UntexturedLineTransient line : lines) {
            linePositionColor(consumer, pose, line.x0(), line.y0(), line.z0(), line.x1(), line.y1(), line.z1(),
                    line.color(), line.alpha());
        }
    }

    private static void addBoxLine(List<LegacyWavefrontModel.UntexturedLineTransient> lines,
            double x0, double y0, double z0, double x1, double y1, double z1, int color, int alpha) {
        lines.add(new LegacyWavefrontModel.UntexturedLineTransient(
                x0, y0, z0, x1, y1, z1, color & 0xFFFFFF, clampAlpha(alpha)));
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

    private static void vertexPositionColorIdentity(VertexConsumer consumer, double x, double y, double z,
            int color, int alpha) {
        consumer.vertex((float) x, (float) y, (float) z)
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, clampAlpha(alpha))
                .endVertex();
    }

    private static int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private LegacyLineRenderer() {
    }
}
