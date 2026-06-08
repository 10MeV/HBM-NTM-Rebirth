package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3d;

import java.util.Random;

/**
 * Modern bridge for the old BeamPronter SOLID beam path.
 */
public final class LegacyBeamRenderer {
    public enum WaveType {
        RANDOM,
        SPIRAL
    }

    private static final Random RANDOM = new Random();

    public static void solidBeam(PoseStack poseStack, MultiBufferSource buffer,
                                 double x, double y, double z,
                                 WaveType wave, int outerColor, int innerColor,
                                 int start, int segments, float size, int layers, float thickness) {
        solidBeam(poseStack, buffer, false, x, y, z, wave, outerColor, innerColor, start, segments, size, layers, thickness);
    }

    public static void solidBeamWithDepth(PoseStack poseStack, MultiBufferSource buffer,
                                 double x, double y, double z,
                                 WaveType wave, int outerColor, int innerColor,
                                 int start, int segments, float size, int layers, float thickness) {
        solidBeam(poseStack, buffer, true, x, y, z, wave, outerColor, innerColor, start, segments, size, layers, thickness);
    }

    public static void solidBeam(PoseStack poseStack, MultiBufferSource buffer, boolean depthWrite,
                                 double x, double y, double z,
                                 WaveType wave, int outerColor, int innerColor,
                                 int start, int segments, float size, int layers, float thickness) {
        if (segments <= 0 || layers <= 0) {
            return;
        }

        Vector3d skeleton = new Vector3d(x, y, z);
        double length = skeleton.length();
        if (length <= 1.0E-5D) {
            return;
        }

        Vector3d axisY = new Vector3d(skeleton).normalize();
        Vector3d axisX = perpendicular(axisY);
        Vector3d axisZ = new Vector3d(axisY).cross(axisX).normalize();
        VertexConsumer consumer = buffer.getBuffer(depthWrite
                ? LegacyUntexturedQuadRenderer.additiveDepthWriteNoCullType()
                : LegacyUntexturedQuadRenderer.additiveNoCullType());
        PoseStack.Pose pose = poseStack.last();

        RANDOM.setSeed(start);
        double segmentLength = length / segments;
        Vector3d previous = point(axisY, axisX, axisZ, segmentLength, size, wave, start, 0);
        for (int i = 1; i <= segments; i++) {
            Vector3d current = point(axisY, axisX, axisZ, segmentLength, size, wave, start, i);
            renderSegment(consumer, pose, previous, current, axisX, axisZ, outerColor, innerColor, layers, thickness);
            previous = current;
        }
    }

    public static void lineBeam(PoseStack poseStack, MultiBufferSource buffer,
                                double x, double y, double z,
                                WaveType wave, int outerColor, int innerColor,
                                int start, int segments, float size) {
        if (segments <= 0) {
            return;
        }

        Vector3d skeleton = new Vector3d(x, y, z);
        double length = skeleton.length();
        if (length <= 1.0E-5D) {
            return;
        }

        Vector3d axisY = new Vector3d(skeleton).normalize();
        Vector3d axisX = perpendicular(axisY);
        Vector3d axisZ = new Vector3d(axisY).cross(axisX).normalize();
        VertexConsumer consumer = LegacyLineRenderer.consumer(buffer, LegacyLineRenderer.DEFAULT_LINE_WIDTH,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
        PoseStack.Pose pose = poseStack.last();

        RANDOM.setSeed(start);
        double segmentLength = length / segments;
        Vector3d previous = point(axisY, axisX, axisZ, segmentLength, size, wave, start, 0);
        for (int i = 1; i <= segments; i++) {
            Vector3d current = point(axisY, axisX, axisZ, segmentLength, size, wave, start, i);
            line(consumer, pose, previous, current, outerColor);
            previous = current;
        }
        line(consumer, pose, new Vector3d(), skeleton, innerColor);
    }

    private static Vector3d perpendicular(Vector3d axisY) {
        Vector3d fallback = Math.abs(axisY.y) < 0.9D ? new Vector3d(0.0D, 1.0D, 0.0D) : new Vector3d(1.0D, 0.0D, 0.0D);
        return fallback.cross(axisY).normalize();
    }

    private static Vector3d point(Vector3d axisY, Vector3d axisX, Vector3d axisZ,
                                  double segmentLength, float size, WaveType wave, int start, int index) {
        double angle;
        if (wave == WaveType.SPIRAL) {
            angle = Math.toRadians(start + 45.0D * index);
        } else {
            angle = Math.PI * 2.0D * RANDOM.nextFloat();
            angle += Math.PI * 2.0D * RANDOM.nextFloat();
        }

        return new Vector3d(axisY).mul(segmentLength * index)
                .add(new Vector3d(axisX).mul(Math.cos(angle) * size))
                .add(new Vector3d(axisZ).mul(Math.sin(angle) * size));
    }

    private static void renderSegment(VertexConsumer consumer, PoseStack.Pose pose,
                                      Vector3d previous, Vector3d current,
                                      Vector3d axisX, Vector3d axisZ,
                                      int outerColor, int innerColor,
                                      int layers, float thickness) {
        float radius = thickness / layers;
        for (int layer = 1; layer <= layers; layer++) {
            float inter = layers == 1 ? 0.0F : (float) (layer - 1) / (float) (layers - 1);
            int color = interpolateColor(outerColor, innerColor, inter);
            double offset = radius * layer;
            Vector3d xOffset = new Vector3d(axisX).mul(offset);
            Vector3d zOffset = new Vector3d(axisZ).mul(offset);

            quad(consumer, pose,
                    new Vector3d(previous).add(xOffset).add(zOffset),
                    new Vector3d(previous).add(xOffset).sub(zOffset),
                    new Vector3d(current).add(xOffset).sub(zOffset),
                    new Vector3d(current).add(xOffset).add(zOffset),
                    color);
            quad(consumer, pose,
                    new Vector3d(previous).sub(xOffset).add(zOffset),
                    new Vector3d(previous).sub(xOffset).sub(zOffset),
                    new Vector3d(current).sub(xOffset).sub(zOffset),
                    new Vector3d(current).sub(xOffset).add(zOffset),
                    color);
            quad(consumer, pose,
                    new Vector3d(previous).add(xOffset).add(zOffset),
                    new Vector3d(previous).sub(xOffset).add(zOffset),
                    new Vector3d(current).sub(xOffset).add(zOffset),
                    new Vector3d(current).add(xOffset).add(zOffset),
                    color);
            quad(consumer, pose,
                    new Vector3d(previous).add(xOffset).sub(zOffset),
                    new Vector3d(previous).sub(xOffset).sub(zOffset),
                    new Vector3d(current).sub(xOffset).sub(zOffset),
                    new Vector3d(current).add(xOffset).sub(zOffset),
                    color);
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             Vector3d v0, Vector3d v1, Vector3d v2, Vector3d v3, int color) {
        LegacyUntexturedQuadRenderer.quad(
                consumer,
                pose,
                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,
                v3.x, v3.y, v3.z,
                red(color), green(color), blue(color),
                255, 255, 255, 255);
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose, Vector3d start, Vector3d end, int color) {
        LegacyLineRenderer.line(consumer, pose, start.x, start.y, start.z, end.x, end.y, end.z, color, 255);
    }

    private static int interpolateColor(int outerColor, int innerColor, float inter) {
        int red = (int) (red(outerColor) + (red(innerColor) - red(outerColor)) * inter);
        int green = (int) (green(outerColor) + (green(innerColor) - green(outerColor)) * inter);
        int blue = (int) (blue(outerColor) + (blue(innerColor) - blue(outerColor)) * inter);
        return red << 16 | green << 8 | blue;
    }

    private static int red(int color) {
        return color >> 16 & 255;
    }

    private static int green(int color) {
        return color >> 8 & 255;
    }

    private static int blue(int color) {
        return color & 255;
    }

    private LegacyBeamRenderer() {
    }
}
