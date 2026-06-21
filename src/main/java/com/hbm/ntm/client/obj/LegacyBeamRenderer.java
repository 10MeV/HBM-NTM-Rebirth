package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modern bridge for the old BeamPronter SOLID beam path.
 */
public final class LegacyBeamRenderer {
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

        double segmentLength = length / segments;
        List<BeamPoint> points = beamPoints(axisY, axisX, axisZ, segmentLength, size, wave, start, segments);
        for (int i = 1; i < points.size(); i++) {
            Vector3d previous = points.get(i - 1).asVector();
            Vector3d current = points.get(i).asVector();
            renderSegment(consumer, pose, previous, current, axisX, axisZ, outerColor, innerColor, layers, thickness);
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

        double segmentLength = length / segments;
        List<BeamPoint> points = beamPoints(axisY, axisX, axisZ, segmentLength, size, wave, start, segments);
        for (int i = 1; i < points.size(); i++) {
            Vector3d previous = points.get(i - 1).asVector();
            Vector3d current = points.get(i).asVector();
            line(consumer, pose, previous, current, outerColor);
        }
        line(consumer, pose, new Vector3d(), skeleton, innerColor);
    }

    private static void solidBeam(PoseStack poseStack, MultiBufferSource buffer, BeamPlan plan) {
        if (plan.solidSegments().isEmpty() || plan.orientation().length() <= 1.0E-5D) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(plan.depthWrite()
                ? LegacyUntexturedQuadRenderer.additiveDepthWriteNoCullType()
                : LegacyUntexturedQuadRenderer.additiveNoCullType());
        PoseStack.Pose pose = poseStack.last();

        for (BeamSegmentPlan segment : plan.solidSegments()) {
            for (BeamLayerPlan layer : segment.layers()) {
                for (BeamQuadPlan quad : layer.quads()) {
                    quad(consumer, pose, quad.v0(), quad.v1(), quad.v2(), quad.v3(), layer.color());
                }
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
            line(consumer, pose, line.start(), line.end(), line.color());
        }
        if (plan.centralLine() != null) {
            line(consumer, pose, plan.centralLine().start(), plan.centralLine().end(), plan.centralLine().color());
        }
    }

    public static List<BeamPoint> beamPoints(double x, double y, double z,
            WaveType wave, int start, int segments, float size) {
        if (segments <= 0) {
            return List.of();
        }
        Vector3d skeleton = new Vector3d(x, y, z);
        double length = skeleton.length();
        if (length <= 1.0E-5D) {
            return List.of();
        }
        Vector3d axisY = new Vector3d(skeleton).normalize();
        Vector3d axisX = perpendicular(axisY);
        Vector3d axisZ = new Vector3d(axisY).cross(axisX).normalize();
        return beamPoints(axisY, axisX, axisZ, length / segments, size, wave, start, segments);
    }

    private static List<BeamPoint> beamPoints(Vector3d axisY, Vector3d axisX, Vector3d axisZ,
            double segmentLength, float size, WaveType wave, int start, int segments) {
        Random random = new Random(start);
        List<BeamPoint> points = new ArrayList<>(segments + 1);
        for (int i = 0; i <= segments; i++) {
            points.add(point(axisY, axisX, axisZ, segmentLength, size, wave, start, i, random));
        }
        return points;
    }

    private static Vector3d perpendicular(Vector3d axisY) {
        Vector3d fallback = Math.abs(axisY.y) < 0.9D ? new Vector3d(0.0D, 1.0D, 0.0D) : new Vector3d(1.0D, 0.0D, 0.0D);
        return fallback.cross(axisY).normalize();
    }

    private static BeamPoint point(Vector3d axisY, Vector3d axisX, Vector3d axisZ,
                                  double segmentLength, float size, WaveType wave, int start, int index, Random random) {
        double angle;
        if (wave == WaveType.SPIRAL) {
            angle = Math.toRadians(start + 45.0D * index);
        } else {
            angle = Math.PI * 2.0D * random.nextFloat();
            angle += Math.PI * 2.0D * random.nextFloat();
        }

        Vector3d point = new Vector3d(axisY).mul(segmentLength * index)
                .add(new Vector3d(axisX).mul(Math.cos(angle) * size))
                .add(new Vector3d(axisZ).mul(Math.sin(angle) * size));
        return new BeamPoint(index, point.x, point.y, point.z);
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
        List<BeamPoint> points = beamPoints(x, y, z, wave, start, safeSegments, size);
        BeamAxesPlan axes = axesPlan(x, y, z);
        return new BeamPlan(x, y, z, wave, beamType, outerColor, innerColor, start, safeSegments,
                size, safeLayers, thickness, depthWrite, orientationPlan(x, y, z),
                statePlan(beamType, depthWrite), layerColors(outerColor, innerColor, safeLayers),
                points, beamSegments(points, axes, outerColor, innerColor, safeLayers, thickness),
                lineSegments(points, outerColor), centralLinePlan(x, y, z, innerColor));
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
        Vector3d skeleton = new Vector3d(x, y, z);
        if (skeleton.lengthSquared() <= 1.0E-10D) {
            return new BeamAxesPlan(new BeamVector(0.0D, 1.0D, 0.0D),
                    new BeamVector(1.0D, 0.0D, 0.0D), new BeamVector(0.0D, 0.0D, 1.0D));
        }
        Vector3d axisY = skeleton.normalize();
        Vector3d axisX = perpendicular(axisY);
        Vector3d axisZ = new Vector3d(axisY).cross(axisX).normalize();
        return new BeamAxesPlan(BeamVector.from(axisY), BeamVector.from(axisX), BeamVector.from(axisZ));
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
        if (points == null || points.size() < 2 || layers <= 0 || axes == null) {
            return List.of();
        }
        List<BeamSegmentPlan> segments = new ArrayList<>(points.size() - 1);
        for (int i = 1; i < points.size(); i++) {
            segments.add(beamSegment(i - 1, points.get(i - 1), points.get(i), axes,
                    outerColor, innerColor, layers, thickness));
        }
        return List.copyOf(segments);
    }

    private static BeamSegmentPlan beamSegment(int segmentIndex, BeamPoint previous, BeamPoint current,
            BeamAxesPlan axes, int outerColor, int innerColor, int layers, float thickness) {
        List<BeamLayerPlan> layerPlans = new ArrayList<>(layers);
        float radius = thickness / layers;
        for (int layer = 1; layer <= layers; layer++) {
            float inter = layers == 1 ? 0.0F : (float) (layer - 1) / (float) (layers - 1);
            int color = interpolateColor(outerColor, innerColor, inter);
            double offset = radius * layer;
            BeamVector xOffset = axes.axisX().scale(offset);
            BeamVector zOffset = axes.axisZ().scale(offset);
            BeamVector last = BeamVector.from(previous);
            BeamVector next = BeamVector.from(current);
            layerPlans.add(new BeamLayerPlan(layer, offset, color, List.of(
                    new BeamQuadPlan(0, last.add(xOffset).add(zOffset), last.add(xOffset).subtract(zOffset),
                            next.add(xOffset).subtract(zOffset), next.add(xOffset).add(zOffset)),
                    new BeamQuadPlan(1, last.subtract(xOffset).add(zOffset), last.subtract(xOffset).subtract(zOffset),
                            next.subtract(xOffset).subtract(zOffset), next.subtract(xOffset).add(zOffset)),
                    new BeamQuadPlan(2, last.add(xOffset).add(zOffset), last.subtract(xOffset).add(zOffset),
                            next.subtract(xOffset).add(zOffset), next.add(xOffset).add(zOffset)),
                    new BeamQuadPlan(3, last.add(xOffset).subtract(zOffset), last.subtract(xOffset).subtract(zOffset),
                            next.subtract(xOffset).subtract(zOffset), next.add(xOffset).subtract(zOffset)))));
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

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             BeamVector v0, BeamVector v1, BeamVector v2, BeamVector v3, int color) {
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

    private static void line(VertexConsumer consumer, PoseStack.Pose pose, BeamVector start, BeamVector end, int color) {
        LegacyLineRenderer.line(consumer, pose, start.x, start.y, start.z, end.x, end.y, end.z, color, 255);
    }

    public static int interpolateColor(int outerColor, int innerColor, float inter) {
        int red = (int) (red(outerColor) + (red(innerColor) - red(outerColor)) * inter);
        int green = (int) (green(outerColor) + (green(innerColor) - green(outerColor)) * inter);
        int blue = (int) (blue(outerColor) + (blue(innerColor) - blue(outerColor)) * inter);
        return red << 16 | green << 8 | blue;
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

    public record BeamLayerPlan(int layer, double radius, int color, List<BeamQuadPlan> quads) {
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
