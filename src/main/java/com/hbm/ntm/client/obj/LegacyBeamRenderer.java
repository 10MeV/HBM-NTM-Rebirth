package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.hbm.ntm.client.render.LegacyRenderRandom;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modern bridge for the old BeamPronter SOLID beam path.
 */
public final class LegacyBeamRenderer {
    private static final double DEG_TO_RAD = Math.PI / 180.0D;
    private static final ThreadLocal<double[]> PRIMITIVE_AXES = ThreadLocal.withInitial(() -> new double[6]);
    private static final ThreadLocal<double[]> PRIMITIVE_POINTS = ThreadLocal.withInitial(() -> new double[0]);

    public enum WaveType {
        RANDOM,
        SPIRAL
    }

    public enum BeamType {
        SOLID,
        LINE
    }

    public static void beam(PoseStack poseStack, MultiBufferSource buffer, BeamPlan plan) {
        if (plan.beamType() == BeamType.SOLID) {
            solidBeam(poseStack, buffer, plan);
            return;
        }
        lineBeam(poseStack, buffer, plan);
    }

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
        if (!solidBeamRenderable(x, y, z, segments, layers)) {
            return;
        }
        LegacyTexturedRenderMode renderMode = solidBeamRenderMode(depthWrite);
        solidBeam(LegacyUntexturedQuadRenderer.consumer(buffer, renderMode, 255), poseStack.last(),
                x, y, z, wave, outerColor, innerColor, start, segments, size, layers, thickness);
    }

    public static DirectSolidBeamBatch directSolidBeamBatch(PoseStack poseStack, MultiBufferSource buffer,
            boolean depthWrite) {
        return new DirectSolidBeamBatch(poseStack, buffer, depthWrite);
    }

    public static void solidBeam(DirectSolidBeamBatch batch,
                                 double x, double y, double z,
                                 WaveType wave, int outerColor, int innerColor,
                                 int start, int segments, float size, int layers, float thickness) {
        solidBeam(batch.consumer, batch.pose, x, y, z, wave, outerColor, innerColor,
                start, segments, size, layers, thickness);
    }

    private static void solidBeam(VertexConsumer consumer, PoseStack.Pose pose,
                                  double x, double y, double z,
                                  WaveType wave, int outerColor, int innerColor,
                                  int start, int segments, float size, int layers, float thickness) {
        solidBeam(consumer, pose.pose(), x, y, z, wave, outerColor, innerColor,
                start, segments, size, layers, thickness);
    }

    public static void solidBeam(VertexConsumer consumer, Matrix4f pose,
                                  double x, double y, double z,
                                  WaveType wave, int outerColor, int innerColor,
                                  int start, int segments, float size, int layers, float thickness) {
        if (segments <= 0 || layers <= 0) {
            return;
        }

        double length = Math.sqrt(x * x + y * y + z * z);
        if (length <= 1.0E-5D) {
            return;
        }

        double axisYx = x / length;
        double axisYy = y / length;
        double axisYz = z / length;
        double[] axes = primitiveAxes(axisYx, axisYy, axisYz);
        double axisXx = axes[0];
        double axisXy = axes[1];
        double axisXz = axes[2];
        double axisZx = axes[3];
        double axisZy = axes[4];
        double axisZz = axes[5];
        double segmentLength = length / segments;

        double[] points = primitivePoints(segments + 1);
        Random random = LegacyRenderRandom.seeded(start);
        for (int i = 0; i <= segments; i++) {
            double angle = beamPointAngle(wave, start, i, random);
            double angleCos = Math.cos(angle);
            double angleSin = Math.sin(angle);
            int pointOffset = i * 3;
            points[pointOffset] = beamPoint(axisYx, axisXx, axisZx, segmentLength, size, i, angleCos, angleSin);
            points[pointOffset + 1] = beamPoint(axisYy, axisXy, axisZy, segmentLength, size, i, angleCos, angleSin);
            points[pointOffset + 2] = beamPoint(axisYz, axisXz, axisZz, segmentLength, size, i, angleCos, angleSin);
        }

        float radius = thickness / layers;
        for (int layer = 1; layer <= layers; layer++) {
            double offset = radius * layer;
            double xOffsetX = axisXx * offset;
            double xOffsetY = axisXy * offset;
            double xOffsetZ = axisXz * offset;
            double zOffsetX = axisZx * offset;
            double zOffsetY = axisZy * offset;
            double zOffsetZ = axisZz * offset;
            int color = layerColor(outerColor, innerColor, layer, layers);
            for (int i = 1; i <= segments; i++) {
                int prevOffset = (i - 1) * 3;
                int currentOffset = i * 3;
                emitSegmentQuads(consumer, pose,
                        points[prevOffset], points[prevOffset + 1], points[prevOffset + 2],
                        points[currentOffset], points[currentOffset + 1], points[currentOffset + 2],
                        xOffsetX, xOffsetY, xOffsetZ, zOffsetX, zOffsetY, zOffsetZ, color);
            }
        }
    }

    public static void lineBeam(PoseStack poseStack, MultiBufferSource buffer,
                                double x, double y, double z,
                                WaveType wave, int outerColor, int innerColor,
                                int start, int segments, float size) {
        if (!lineBeamRenderable(x, y, z, segments)) {
            return;
        }
        VertexConsumer consumer = LegacyLineRenderer.consumer(buffer, LegacyLineRenderer.DEFAULT_LINE_WIDTH,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
        lineBeam(consumer, poseStack.last(), 0.0D, 0.0D, 0.0D, x, y, z, wave, outerColor, innerColor,
                start, segments, size);
    }

    public static DirectLineBeamBatch directLineBeamBatch(PoseStack poseStack, MultiBufferSource buffer) {
        return new DirectLineBeamBatch(poseStack, buffer);
    }

    public static void lineBeam(DirectLineBeamBatch batch,
                                double x, double y, double z,
                                WaveType wave, int outerColor, int innerColor,
                                int start, int segments, float size) {
        lineBeam(batch.consumer, batch.pose, 0.0D, 0.0D, 0.0D, x, y, z, wave, outerColor, innerColor,
                start, segments, size);
    }

    public static void lineBeam(DirectLineBeamBatch batch,
                                double originX, double originY, double originZ,
                                double x, double y, double z,
                                WaveType wave, int outerColor, int innerColor,
                                int start, int segments, float size) {
        lineBeam(batch.consumer, batch.pose, originX, originY, originZ, x, y, z, wave, outerColor, innerColor,
                start, segments, size);
    }

    private static void lineBeam(VertexConsumer consumer, PoseStack.Pose pose,
                                double originX, double originY, double originZ,
                                double x, double y, double z,
                                WaveType wave, int outerColor, int innerColor,
                                int start, int segments, float size) {
        lineBeam(consumer, pose.pose(), pose.normal(), originX, originY, originZ, x, y, z, wave,
                outerColor, innerColor, start, segments, size);
    }

    public static void lineBeam(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                                double originX, double originY, double originZ,
                                double x, double y, double z,
                                WaveType wave, int outerColor, int innerColor,
                                int start, int segments, float size) {
        if (segments <= 0) {
            return;
        }

        double length = Math.sqrt(x * x + y * y + z * z);
        if (length <= 1.0E-5D) {
            return;
        }

        double axisYx = x / length;
        double axisYy = y / length;
        double axisYz = z / length;
        double[] axes = primitiveAxes(axisYx, axisYy, axisYz);
        double axisXx = axes[0];
        double axisXy = axes[1];
        double axisXz = axes[2];
        double axisZx = axes[3];
        double axisZy = axes[4];
        double axisZz = axes[5];
        double segmentLength = length / segments;
        Random random = LegacyRenderRandom.seeded(start);
        double angle = beamPointAngle(wave, start, 0, random);
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        double prevX = beamPoint(axisYx, axisXx, axisZx, segmentLength, size, 0, angleCos, angleSin);
        double prevY = beamPoint(axisYy, axisXy, axisZy, segmentLength, size, 0, angleCos, angleSin);
        double prevZ = beamPoint(axisYz, axisXz, axisZz, segmentLength, size, 0, angleCos, angleSin);
        for (int i = 1; i <= segments; i++) {
            angle = beamPointAngle(wave, start, i, random);
            angleCos = Math.cos(angle);
            angleSin = Math.sin(angle);
            double currentX = beamPoint(axisYx, axisXx, axisZx, segmentLength, size, i, angleCos, angleSin);
            double currentY = beamPoint(axisYy, axisXy, axisZy, segmentLength, size, i, angleCos, angleSin);
            double currentZ = beamPoint(axisYz, axisXz, axisZz, segmentLength, size, i, angleCos, angleSin);
            LegacyLineRenderer.line(consumer, pose, normal,
                    originX + currentX, originY + currentY, originZ + currentZ,
                    originX + prevX, originY + prevY, originZ + prevZ,
                    outerColor, 255);
            prevX = currentX;
            prevY = currentY;
            prevZ = currentZ;
        }
        LegacyLineRenderer.line(consumer, pose, normal, originX, originY, originZ,
                originX + x, originY + y, originZ + z, innerColor, 255);
    }

    private static void solidBeam(PoseStack poseStack, MultiBufferSource buffer, BeamPlan plan) {
        if (plan.solidSegments().isEmpty() || plan.orientation().length() <= 1.0E-5D) {
            return;
        }

        LegacyTexturedRenderMode renderMode = solidBeamRenderMode(plan.depthWrite());
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.consumer(buffer, renderMode, 255);
        PoseStack.Pose pose = poseStack.last();

        for (BeamSegmentPlan segment : plan.solidSegments()) {
            for (BeamLayerPlan layer : segment.layers()) {
                renderBeamQuads(consumer, pose, layer);
            }
        }
    }

    private static void lineBeam(PoseStack poseStack, MultiBufferSource buffer, BeamPlan plan) {
        if ((plan.lineSegments().isEmpty() && plan.centralLine() == null)
                || plan.orientation().length() <= 1.0E-5D) {
            return;
        }

        VertexConsumer consumer = LegacyLineRenderer.consumer(buffer, LegacyLineRenderer.DEFAULT_LINE_WIDTH,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
        PoseStack.Pose pose = poseStack.last();
        for (BeamLinePlan line : plan.lineSegments()) {
            renderLinePlan(consumer, pose, line);
        }
        if (plan.centralLine() != null) {
            renderLinePlan(consumer, pose, plan.centralLine());
        }
    }

    public static List<BeamPoint> beamPoints(double x, double y, double z,
            WaveType wave, int start, int segments, float size) {
        if (segments <= 0) {
            return List.of();
        }
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length <= 1.0E-5D) {
            return List.of();
        }
        double axisYx = x / length;
        double axisYy = y / length;
        double axisYz = z / length;
        double[] axes = primitiveAxes(axisYx, axisYy, axisYz);
        double axisXx = axes[0];
        double axisXy = axes[1];
        double axisXz = axes[2];
        double axisZx = axes[3];
        double axisZy = axes[4];
        double axisZz = axes[5];
        double segmentLength = length / segments;
        Random random = LegacyRenderRandom.seeded(start);
        List<BeamPoint> points = new ArrayList<>(segments + 1);
        for (int i = 0; i <= segments; i++) {
            double angle = beamPointAngle(wave, start, i, random);
            double angleCos = Math.cos(angle);
            double angleSin = Math.sin(angle);
            points.add(new BeamPoint(i,
                    beamPoint(axisYx, axisXx, axisZx, segmentLength, size, i, angleCos, angleSin),
                    beamPoint(axisYy, axisXy, axisZy, segmentLength, size, i, angleCos, angleSin),
                    beamPoint(axisYz, axisXz, axisZz, segmentLength, size, i, angleCos, angleSin)));
        }
        return points;
    }

    private static double[] primitiveAxes(double axisYx, double axisYy, double axisYz) {
        double[] axes = PRIMITIVE_AXES.get();
        double fallbackX = Math.abs(axisYy) < 0.9D ? 0.0D : 1.0D;
        double fallbackY = Math.abs(axisYy) < 0.9D ? 1.0D : 0.0D;
        double fallbackZ = 0.0D;
        double axisXx = fallbackY * axisYz - fallbackZ * axisYy;
        double axisXy = fallbackZ * axisYx - fallbackX * axisYz;
        double axisXz = fallbackX * axisYy - fallbackY * axisYx;
        double axisXLength = Math.sqrt(axisXx * axisXx + axisXy * axisXy + axisXz * axisXz);
        if (axisXLength <= 1.0E-10D) {
            axisXx = 1.0D;
            axisXy = 0.0D;
            axisXz = 0.0D;
        } else {
            axisXx /= axisXLength;
            axisXy /= axisXLength;
            axisXz /= axisXLength;
        }
        double axisZx = axisYy * axisXz - axisYz * axisXy;
        double axisZy = axisYz * axisXx - axisYx * axisXz;
        double axisZz = axisYx * axisXy - axisYy * axisXx;
        double axisZLength = Math.sqrt(axisZx * axisZx + axisZy * axisZy + axisZz * axisZz);
        if (axisZLength <= 1.0E-10D) {
            axisZx = 0.0D;
            axisZy = 0.0D;
            axisZz = 1.0D;
        } else {
            axisZx /= axisZLength;
            axisZy /= axisZLength;
            axisZz /= axisZLength;
        }
        axes[0] = axisXx;
        axes[1] = axisXy;
        axes[2] = axisXz;
        axes[3] = axisZx;
        axes[4] = axisZy;
        axes[5] = axisZz;
        return axes;
    }

    private static double[] primitivePoints(int pointCount) {
        int required = Math.max(0, pointCount) * 3;
        double[] points = PRIMITIVE_POINTS.get();
        if (points.length >= required) {
            return points;
        }
        int capacity = 3;
        while (capacity < required) {
            capacity <<= 1;
        }
        points = new double[capacity];
        PRIMITIVE_POINTS.set(points);
        return points;
    }

    private static double beamPointAngle(WaveType wave, int start, int index, Random random) {
        if (wave == WaveType.SPIRAL) {
            return (start + 45.0D * index) * DEG_TO_RAD;
        }
        return Math.PI * 2.0D * random.nextFloat() + Math.PI * 2.0D * random.nextFloat();
    }

    private static double beamPoint(double axisY, double axisX, double axisZ,
            double segmentLength, float size, int index, double angleCos, double angleSin) {
        return axisY * segmentLength * index + axisX * angleCos * size + axisZ * angleSin * size;
    }

    private static void emitSegmentQuads(VertexConsumer consumer, Matrix4f pose,
            double lastX, double lastY, double lastZ, double nextX, double nextY, double nextZ,
            double xOffsetX, double xOffsetY, double xOffsetZ, double zOffsetX, double zOffsetY, double zOffsetZ,
            int color) {
        LegacyUntexturedQuadRenderer.quad(consumer, pose,
                lastX + xOffsetX + zOffsetX, lastY + xOffsetY + zOffsetY, lastZ + xOffsetZ + zOffsetZ,
                lastX + xOffsetX - zOffsetX, lastY + xOffsetY - zOffsetY, lastZ + xOffsetZ - zOffsetZ,
                nextX + xOffsetX - zOffsetX, nextY + xOffsetY - zOffsetY, nextZ + xOffsetZ - zOffsetZ,
                nextX + xOffsetX + zOffsetX, nextY + xOffsetY + zOffsetY, nextZ + xOffsetZ + zOffsetZ,
                color, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quad(consumer, pose,
                lastX - xOffsetX + zOffsetX, lastY - xOffsetY + zOffsetY, lastZ - xOffsetZ + zOffsetZ,
                lastX - xOffsetX - zOffsetX, lastY - xOffsetY - zOffsetY, lastZ - xOffsetZ - zOffsetZ,
                nextX - xOffsetX - zOffsetX, nextY - xOffsetY - zOffsetY, nextZ - xOffsetZ - zOffsetZ,
                nextX - xOffsetX + zOffsetX, nextY - xOffsetY + zOffsetY, nextZ - xOffsetZ + zOffsetZ,
                color, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quad(consumer, pose,
                lastX + xOffsetX + zOffsetX, lastY + xOffsetY + zOffsetY, lastZ + xOffsetZ + zOffsetZ,
                lastX - xOffsetX + zOffsetX, lastY - xOffsetY + zOffsetY, lastZ - xOffsetZ + zOffsetZ,
                nextX - xOffsetX + zOffsetX, nextY - xOffsetY + zOffsetY, nextZ - xOffsetZ + zOffsetZ,
                nextX + xOffsetX + zOffsetX, nextY + xOffsetY + zOffsetY, nextZ + xOffsetZ + zOffsetZ,
                color, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quad(consumer, pose,
                lastX + xOffsetX - zOffsetX, lastY + xOffsetY - zOffsetY, lastZ + xOffsetZ - zOffsetZ,
                lastX - xOffsetX - zOffsetX, lastY - xOffsetY - zOffsetY, lastZ - xOffsetZ - zOffsetZ,
                nextX - xOffsetX - zOffsetX, nextY - xOffsetY - zOffsetY, nextZ - xOffsetZ - zOffsetZ,
                nextX + xOffsetX - zOffsetX, nextY + xOffsetY - zOffsetY, nextZ + xOffsetZ - zOffsetZ,
                color, 255, 255, 255, 255);
    }

    public static BeamPlan beamPlan(double x, double y, double z,
            WaveType wave, BeamType beamType, int outerColor, int innerColor,
            int start, int segments, float size, int layers, float thickness) {
        return beamPlan(false, x, y, z, wave, beamType, outerColor, innerColor, start, segments, size, layers, thickness);
    }

    public static BeamPlan beamPlanWithDepth(double x, double y, double z,
            WaveType wave, BeamType beamType, int outerColor, int innerColor,
            int start, int segments, float size, int layers, float thickness) {
        return beamPlan(true, x, y, z, wave, beamType, outerColor, innerColor, start, segments, size, layers, thickness);
    }

    public static BeamPlan beamPlan(boolean depthWrite, double x, double y, double z,
            WaveType wave, BeamType beamType, int outerColor, int innerColor,
            int start, int segments, float size, int layers, float thickness) {
        int safeSegments = Math.max(0, segments);
        int safeLayers = Math.max(0, layers);
        BeamOrientationPlan orientation = orientationPlan(x, y, z);
        List<BeamPoint> points = safeSegments > 0 && orientation.length() > 1.0E-5D
                ? beamPoints(x, y, z, wave, start, safeSegments, size)
                : List.of();
        List<Integer> layerColors = layerColors(outerColor, innerColor, safeLayers);
        List<BeamSegmentPlan> solidSegments = List.of();
        List<BeamLinePlan> lineSegments = List.of();
        BeamLinePlan centralLine = null;
        if (beamType == BeamType.SOLID) {
            if (points.size() >= 2 && !layerColors.isEmpty()) {
                solidSegments = beamSegments(points, axesPlan(x, y, z), layerColors, thickness);
            }
        } else if (beamType == BeamType.LINE) {
            if (points.size() >= 2) {
                lineSegments = lineSegments(points, outerColor);
            }
            centralLine = centralLinePlan(x, y, z, innerColor);
        }
        return new BeamPlan(x, y, z, wave, beamType, outerColor, innerColor, start, safeSegments,
                size, safeLayers, thickness, depthWrite, orientation,
                statePlan(beamType, depthWrite), layerColors,
                points, solidSegments, lineSegments, centralLine);
    }

    public static BeamOrientationPlan orientationPlan(double x, double y, double z) {
        double horizontal = Math.sqrt(x * x + z * z);
        double yawDegrees = Math.atan2(x, z) * 180.0D / Math.PI;
        double pitchDegrees = Math.atan2(y, horizontal) * 180.0D / Math.PI;
        return new BeamOrientationPlan(yawDegrees, pitchDegrees, 180.0D, pitchDegrees - 90.0D,
                Math.sqrt(x * x + y * y + z * z));
    }

    public static BeamRenderStatePlan statePlan(BeamType beamType, boolean depthWrite) {
        boolean solid = beamType == BeamType.SOLID;
        return new BeamRenderStatePlan(false, false, !solid, solid, solid, depthWrite);
    }

    public static BeamAxesPlan axesPlan(double x, double y, double z) {
        double lengthSquared = x * x + y * y + z * z;
        if (lengthSquared <= 1.0E-10D) {
            return new BeamAxesPlan(new BeamVector(0.0D, 1.0D, 0.0D),
                    new BeamVector(1.0D, 0.0D, 0.0D), new BeamVector(0.0D, 0.0D, 1.0D));
        }
        double length = Math.sqrt(lengthSquared);
        double axisYx = x / length;
        double axisYy = y / length;
        double axisYz = z / length;
        double[] axes = primitiveAxes(axisYx, axisYy, axisYz);
        return new BeamAxesPlan(new BeamVector(axisYx, axisYy, axisYz),
                new BeamVector(axes[0], axes[1], axes[2]), new BeamVector(axes[3], axes[4], axes[5]));
    }

    public static List<Integer> layerColors(int outerColor, int innerColor, int layers) {
        if (layers <= 0) {
            return List.of();
        }
        List<Integer> colors = new ArrayList<>(layers);
        for (int layer = 1; layer <= layers; layer++) {
            float inter = layers == 1 ? 0.0F : (float) (layer - 1) / (float) (layers - 1);
            colors.add(interpolateColor(outerColor, innerColor, inter));
        }
        return List.copyOf(colors);
    }

    public static List<BeamSegmentPlan> beamSegments(List<BeamPoint> points, BeamAxesPlan axes,
            int outerColor, int innerColor, int layers, float thickness) {
        return beamSegments(points, axes, layerColors(outerColor, innerColor, layers), thickness);
    }

    private static List<BeamSegmentPlan> beamSegments(List<BeamPoint> points, BeamAxesPlan axes,
            List<Integer> layerColors, float thickness) {
        if (points == null || points.size() < 2 || layerColors == null || layerColors.isEmpty() || axes == null) {
            return List.of();
        }
        List<BeamSegmentPlan> segments = new ArrayList<>(points.size() - 1);
        for (int i = 1; i < points.size(); i++) {
            segments.add(beamSegment(i - 1, points.get(i - 1), points.get(i), axes, layerColors, thickness));
        }
        return List.copyOf(segments);
    }

    private static BeamSegmentPlan beamSegment(int segmentIndex, BeamPoint previous, BeamPoint current,
            BeamAxesPlan axes, List<Integer> layerColors, float thickness) {
        int layers = layerColors.size();
        List<BeamLayerPlan> layerPlans = new ArrayList<>(layers);
        float radius = thickness / layers;
        for (int layer = 1; layer <= layers; layer++) {
            int color = layerColors.get(layer - 1);
            double offset = radius * layer;
            double xOffsetX = axes.axisX().x() * offset;
            double xOffsetY = axes.axisX().y() * offset;
            double xOffsetZ = axes.axisX().z() * offset;
            double zOffsetX = axes.axisZ().x() * offset;
            double zOffsetY = axes.axisZ().y() * offset;
            double zOffsetZ = axes.axisZ().z() * offset;
            double lastX = previous.x();
            double lastY = previous.y();
            double lastZ = previous.z();
            double nextX = current.x();
            double nextY = current.y();
            double nextZ = current.z();
            layerPlans.add(new BeamLayerPlan(layer, offset, color,
                    new BeamQuadPlan(0,
                            new BeamVector(lastX + xOffsetX + zOffsetX, lastY + xOffsetY + zOffsetY, lastZ + xOffsetZ + zOffsetZ),
                            new BeamVector(lastX + xOffsetX - zOffsetX, lastY + xOffsetY - zOffsetY, lastZ + xOffsetZ - zOffsetZ),
                            new BeamVector(nextX + xOffsetX - zOffsetX, nextY + xOffsetY - zOffsetY, nextZ + xOffsetZ - zOffsetZ),
                            new BeamVector(nextX + xOffsetX + zOffsetX, nextY + xOffsetY + zOffsetY, nextZ + xOffsetZ + zOffsetZ)),
                    new BeamQuadPlan(1,
                            new BeamVector(lastX - xOffsetX + zOffsetX, lastY - xOffsetY + zOffsetY, lastZ - xOffsetZ + zOffsetZ),
                            new BeamVector(lastX - xOffsetX - zOffsetX, lastY - xOffsetY - zOffsetY, lastZ - xOffsetZ - zOffsetZ),
                            new BeamVector(nextX - xOffsetX - zOffsetX, nextY - xOffsetY - zOffsetY, nextZ - xOffsetZ - zOffsetZ),
                            new BeamVector(nextX - xOffsetX + zOffsetX, nextY - xOffsetY + zOffsetY, nextZ - xOffsetZ + zOffsetZ)),
                    new BeamQuadPlan(2,
                            new BeamVector(lastX + xOffsetX + zOffsetX, lastY + xOffsetY + zOffsetY, lastZ + xOffsetZ + zOffsetZ),
                            new BeamVector(lastX - xOffsetX + zOffsetX, lastY - xOffsetY + zOffsetY, lastZ - xOffsetZ + zOffsetZ),
                            new BeamVector(nextX - xOffsetX + zOffsetX, nextY - xOffsetY + zOffsetY, nextZ - xOffsetZ + zOffsetZ),
                            new BeamVector(nextX + xOffsetX + zOffsetX, nextY + xOffsetY + zOffsetY, nextZ + xOffsetZ + zOffsetZ)),
                    new BeamQuadPlan(3,
                            new BeamVector(lastX + xOffsetX - zOffsetX, lastY + xOffsetY - zOffsetY, lastZ + xOffsetZ - zOffsetZ),
                            new BeamVector(lastX - xOffsetX - zOffsetX, lastY - xOffsetY - zOffsetY, lastZ - xOffsetZ - zOffsetZ),
                            new BeamVector(nextX - xOffsetX - zOffsetX, nextY - xOffsetY - zOffsetY, nextZ - xOffsetZ - zOffsetZ),
                            new BeamVector(nextX + xOffsetX - zOffsetX, nextY + xOffsetY - zOffsetY, nextZ + xOffsetZ - zOffsetZ))));
        }
        return new BeamSegmentPlan(segmentIndex, layerPlans);
    }

    public static List<BeamLinePlan> lineSegments(List<BeamPoint> points, int color) {
        if (points == null || points.size() < 2) {
            return List.of();
        }
        List<BeamLinePlan> lines = new ArrayList<>(points.size() - 1);
        for (int i = 1; i < points.size(); i++) {
            lines.add(new BeamLinePlan(i - 1, BeamVector.from(points.get(i)), BeamVector.from(points.get(i - 1)),
                    color & 0xFFFFFF));
        }
        return List.copyOf(lines);
    }

    public static BeamLinePlan centralLinePlan(double x, double y, double z, int color) {
        return new BeamLinePlan(-1, new BeamVector(0.0D, 0.0D, 0.0D), new BeamVector(x, y, z), color & 0xFFFFFF);
    }

    public static LegacyTexturedRenderMode solidBeamRenderMode(boolean depthWrite) {
        return depthWrite ? LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE
                : LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE;
    }

    private static boolean solidBeamRenderable(double x, double y, double z, int segments, int layers) {
        return segments > 0 && layers > 0 && x * x + y * y + z * z > 1.0E-10D;
    }

    private static boolean lineBeamRenderable(double x, double y, double z, int segments) {
        return segments > 0 && x * x + y * y + z * z > 1.0E-10D;
    }

    public static final class DirectSolidBeamBatch {
        private final VertexConsumer consumer;
        private final PoseStack.Pose pose;

        private DirectSolidBeamBatch(PoseStack poseStack, MultiBufferSource buffer, boolean depthWrite) {
            this.consumer = LegacyUntexturedQuadRenderer.consumer(buffer, solidBeamRenderMode(depthWrite), 255);
            this.pose = poseStack.last();
        }
    }

    public static final class DirectLineBeamBatch {
        private final VertexConsumer consumer;
        private final PoseStack.Pose pose;

        private DirectLineBeamBatch(PoseStack poseStack, MultiBufferSource buffer) {
            this.consumer = LegacyLineRenderer.consumer(buffer, LegacyLineRenderer.DEFAULT_LINE_WIDTH,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
            this.pose = poseStack.last();
        }
    }

    private static void renderBeamQuads(VertexConsumer consumer, PoseStack.Pose pose, BeamLayerPlan layer) {
        int color = layer.color() & 0xFFFFFF;
        renderBeamQuad(consumer, pose, color, layer.quad0());
        renderBeamQuad(consumer, pose, color, layer.quad1());
        renderBeamQuad(consumer, pose, color, layer.quad2());
        renderBeamQuad(consumer, pose, color, layer.quad3());
    }

    private static void renderBeamQuad(VertexConsumer consumer, PoseStack.Pose pose, int color, BeamQuadPlan quad) {
        BeamVector v0 = quad.v0();
        BeamVector v1 = quad.v1();
        BeamVector v2 = quad.v2();
        BeamVector v3 = quad.v3();
        LegacyUntexturedQuadRenderer.quad(consumer, pose,
                v0.x(), v0.y(), v0.z(),
                v1.x(), v1.y(), v1.z(),
                v2.x(), v2.y(), v2.z(),
                v3.x(), v3.y(), v3.z(),
                color, 255, 255, 255, 255);
    }

    private static void renderLinePlan(VertexConsumer consumer, PoseStack.Pose pose, BeamLinePlan line) {
        BeamVector start = line.start();
        BeamVector end = line.end();
        LegacyLineRenderer.line(consumer, pose, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(),
                line.color(), 255);
    }

    public static int interpolateColor(int outerColor, int innerColor, float inter) {
        int red = (int) (red(outerColor) + (red(innerColor) - red(outerColor)) * inter);
        int green = (int) (green(outerColor) + (green(innerColor) - green(outerColor)) * inter);
        int blue = (int) (blue(outerColor) + (blue(innerColor) - blue(outerColor)) * inter);
        return red << 16 | green << 8 | blue;
    }

    private static int layerColor(int outerColor, int innerColor, int layer, int layers) {
        float inter = layers == 1 ? 0.0F : (float) (layer - 1) / (float) (layers - 1);
        return interpolateColor(outerColor, innerColor, inter);
    }

    public record BeamPoint(int index, double x, double y, double z) {
        public Vector3d asVector() {
            return new Vector3d(x, y, z);
        }
    }

    public record BeamPlan(double x, double y, double z, WaveType wave, BeamType beamType,
            int outerColor, int innerColor, int start, int segments, float size, int layers,
            float thickness, boolean depthWrite, BeamOrientationPlan orientation,
            BeamRenderStatePlan state, List<Integer> layerColors, List<BeamPoint> points,
            List<BeamSegmentPlan> solidSegments, List<BeamLinePlan> lineSegments, BeamLinePlan centralLine) {
    }

    public record BeamOrientationPlan(double yawDegrees, double pitchDegrees, double initialYawDegrees,
            double xRotationDegrees, double length) {
    }

    public record BeamRenderStatePlan(boolean textureEnabled, boolean lightingEnabled, boolean cullEnabled,
            boolean blendEnabled, boolean additiveBlend, boolean depthWrite) {
    }

    public record BeamAxesPlan(BeamVector axisY, BeamVector axisX, BeamVector axisZ) {
    }

    public record BeamSegmentPlan(int segmentIndex, List<BeamLayerPlan> layers) {
    }

    public record BeamLayerPlan(int layer, double radius, int color,
            BeamQuadPlan quad0, BeamQuadPlan quad1, BeamQuadPlan quad2, BeamQuadPlan quad3) {
    }

    public record BeamQuadPlan(int face, BeamVector v0, BeamVector v1, BeamVector v2, BeamVector v3) {
    }

    public record BeamLinePlan(int segmentIndex, BeamVector start, BeamVector end, int color) {
        public BeamLinePlan(int segmentIndex, BeamVector start, BeamVector end) {
            this(segmentIndex, start, end, 0xFFFFFF);
        }
    }

    public record BeamVector(double x, double y, double z) {
        public static BeamVector from(Vector3d vector) {
            return new BeamVector(vector.x, vector.y, vector.z);
        }

        public static BeamVector from(BeamPoint point) {
            return new BeamVector(point.x(), point.y(), point.z());
        }

        public BeamVector add(BeamVector other) {
            return new BeamVector(x + other.x, y + other.y, z + other.z);
        }

        public BeamVector subtract(BeamVector other) {
            return new BeamVector(x - other.x, y - other.y, z - other.z);
        }

        public BeamVector scale(double scale) {
            return new BeamVector(x * scale, y * scale, z * scale);
        }
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
