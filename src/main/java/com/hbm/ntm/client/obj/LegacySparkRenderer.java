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
        return new SparkRenderPlan(sparkSegments(seed, x, y, z, length, min, max),
                SETUP_LINE_WIDTH, OUTER_LINE_WIDTH, INNER_LINE_WIDTH, colorOuter, colorInner,
                new SparkStatePlan(false, false));
    }

    public static List<SparkSegment> sparkSegments(int seed, double x, double y, double z,
            float length, int min, int max) {
        Random random = new Random(seed);
        double dirX = random.nextDouble() - 0.5D;
        double dirY = random.nextDouble() - 0.5D;
        double dirZ = random.nextDouble() - 0.5D;
        double dirLength = length(dirX, dirY, dirZ);
        if (dirLength <= 1.0E-6D) {
            dirY = 1.0D;
            dirLength = 1.0D;
        }
        dirX /= dirLength;
        dirY /= dirLength;
        dirZ /= dirLength;

        int segments = min + (max > 0 ? random.nextInt(max) : 0);
        List<SparkSegment> result = new ArrayList<>(Math.max(0, segments));
        for (int i = 0; i < segments; i++) {
            double prevX = x;
            double prevY = y;
            double prevZ = z;

            x += dirX * length * random.nextFloat();
            y += dirY * length * random.nextFloat();
            z += dirZ * length * random.nextFloat();

            result.add(new SparkSegment(prevX, prevY, prevZ, x, y, z));
        }
        return result;
    }

    private static double length(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public record SparkSegment(double x0, double y0, double z0, double x1, double y1, double z1) {
    }

    public record SparkRenderPlan(
            List<SparkSegment> segments,
            float setupLineWidth,
            float outerLineWidth,
            float innerLineWidth,
            int outerColor,
            int innerColor,
            SparkStatePlan state) {
    }

    public record SparkStatePlan(boolean textureEnabled, boolean lightingEnabled) {
    }

    private LegacySparkRenderer() {
    }
}
