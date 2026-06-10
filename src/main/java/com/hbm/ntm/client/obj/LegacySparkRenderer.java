package com.hbm.ntm.client.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class LegacySparkRenderer {
    public static final float SETUP_LINE_WIDTH = 3.0F;
    public static final float OUTER_LINE_WIDTH = 5.0F;
    public static final float INNER_LINE_WIDTH = 2.0F;

    public static void renderSpark(ObjRenderContext context, int seed, double x, double y, double z,
            float length, int min, int max, int colorOuter, int colorInner) {
        SparkRenderPlan plan = sparkPlan(seed, x, y, z, length, min, max, colorOuter, colorInner);
        for (SparkSegment segment : plan.segments()) {
            LegacyLineRenderer.line(context, plan.outerLineWidth(), segment.x0(), segment.y0(), segment.z0(),
                    segment.x1(), segment.y1(), segment.z1(), plan.outerColor(), 255);
            LegacyLineRenderer.line(context, plan.innerLineWidth(), segment.x0(), segment.y0(), segment.z0(),
                    segment.x1(), segment.y1(), segment.z1(), plan.innerColor(), 255);
        }
    }

    public static SparkRenderPlan sparkPlan(int seed, double x, double y, double z,
            float length, int min, int max, int colorOuter, int colorInner) {
        Random random = new Random(seed);
        double dirX = random.nextDouble() - 0.5D;
        double dirY = random.nextDouble() - 0.5D;
        double dirZ = random.nextDouble() - 0.5D;
        double dirLength = length(dirX, dirY, dirZ);
        if (dirLength <= 1.0E-6D) {
            dirY = 1.0D;
            dirLength = 1.0D;
        }
        SparkVector rawDirection = new SparkVector(dirX, dirY, dirZ);
        dirX /= dirLength;
        dirY /= dirLength;
        dirZ /= dirLength;
        SparkVector direction = new SparkVector(dirX, dirY, dirZ);

        int segments = min + (max > 0 ? random.nextInt(max) : 0);
        List<SparkStepPlan> steps = new ArrayList<>(Math.max(0, segments));
        List<SparkSegment> result = new ArrayList<>(Math.max(0, segments));
        double currentX = x;
        double currentY = y;
        double currentZ = z;
        for (int i = 0; i < segments; i++) {
            double prevX = currentX;
            double prevY = currentY;
            double prevZ = currentZ;
            float scaleX = random.nextFloat();
            float scaleY = random.nextFloat();
            float scaleZ = random.nextFloat();
            double deltaX = dirX * length * scaleX;
            double deltaY = dirY * length * scaleY;
            double deltaZ = dirZ * length * scaleZ;
            currentX += deltaX;
            currentY += deltaY;
            currentZ += deltaZ;

            SparkSegment segment = new SparkSegment(prevX, prevY, prevZ, currentX, currentY, currentZ);
            steps.add(new SparkStepPlan(i, scaleX, scaleY, scaleZ, new SparkVector(deltaX, deltaY, deltaZ), segment));
            result.add(segment);
        }

        return new SparkRenderPlan(seed, new SparkVector(x, y, z), length, min, max, segments,
                rawDirection, direction, steps, result, SETUP_LINE_WIDTH, OUTER_LINE_WIDTH, INNER_LINE_WIDTH,
                colorOuter, colorInner, new SparkStatePlan(false, false, true, true, true, 3));
    }

    public static List<SparkSegment> sparkSegments(int seed, double x, double y, double z,
            float length, int min, int max) {
        return sparkPlan(seed, x, y, z, length, min, max, 0xFFFFFF, 0xFFFFFF).segments();
    }

    private static double length(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public record SparkSegment(double x0, double y0, double z0, double x1, double y1, double z1) {
    }

    public record SparkVector(double x, double y, double z) {
    }

    public record SparkStepPlan(int index, float scaleX, float scaleY, float scaleZ,
                                SparkVector delta, SparkSegment segment) {
    }

    public record SparkRenderPlan(
            int seed,
            SparkVector origin,
            float length,
            int minSegments,
            int maxRandomSegments,
            int segmentCount,
            SparkVector rawDirection,
            SparkVector initialDirection,
            List<SparkStepPlan> steps,
            List<SparkSegment> segments,
            float setupLineWidth,
            float outerLineWidth,
            float innerLineWidth,
            int outerColor,
            int innerColor,
            SparkStatePlan state) {
    }

    public record SparkStatePlan(boolean textureEnabled, boolean lightingEnabled,
                                 boolean restoresTexture, boolean restoresLighting, boolean pushedMatrix,
                                 int tessellatorDrawMode) {
    }

    private LegacySparkRenderer() {
    }
}
