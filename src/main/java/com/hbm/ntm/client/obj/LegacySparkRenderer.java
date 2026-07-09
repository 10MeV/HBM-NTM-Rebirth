package com.hbm.ntm.client.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.ntm.client.render.LegacyRenderRandom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class LegacySparkRenderer {
    public static final float SETUP_LINE_WIDTH = 3.0F;
    public static final float OUTER_LINE_WIDTH = 5.0F;
    public static final float INNER_LINE_WIDTH = 2.0F;

    public static void renderSpark(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int seed, double x, double y, double z,
            float length, int min, int max, int colorOuter, int colorInner) {
        renderSpark(poseStack.last(), outerLineConsumer(buffer, renderMode), innerLineConsumer(buffer, renderMode),
                seed, x, y, z, length, min, max, colorOuter, colorInner);
    }

    public static VertexConsumer outerLineConsumer(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode) {
        return LegacyLineRenderer.consumer(buffer, OUTER_LINE_WIDTH, renderMode, 255);
    }

    public static VertexConsumer innerLineConsumer(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode) {
        return LegacyLineRenderer.consumer(buffer, INNER_LINE_WIDTH, renderMode, 255);
    }

    public static void renderSpark(PoseStack.Pose pose, VertexConsumer outer, VertexConsumer inner,
            int seed, double x, double y, double z, float length, int min, int max,
            int colorOuter, int colorInner) {
        renderSpark(pose.pose(), pose.normal(), outer, inner, seed, x, y, z, length, min, max,
                colorOuter, colorInner);
    }

    public static void renderSpark(Matrix4f pose, Matrix3f normal, VertexConsumer outer, VertexConsumer inner,
            int seed, double x, double y, double z, float length, int min, int max,
            int colorOuter, int colorInner) {
        Random random = LegacyRenderRandom.seeded(seed);
        double dirX = random.nextDouble() - 0.5D;
        double dirY = random.nextDouble() - 0.5D;
        double dirZ = random.nextDouble() - 0.5D;
        double dirLength = length(dirX, dirY, dirZ);
        if (dirLength <= 1.0E-6D) {
            dirX = 0.0D;
            dirY = 1.0D;
            dirZ = 0.0D;
            dirLength = 1.0D;
        }
        dirX /= dirLength;
        dirY /= dirLength;
        dirZ /= dirLength;

        int segments = min + (max > 0 ? random.nextInt(max) : 0);
        double currentX = x;
        double currentY = y;
        double currentZ = z;
        for (int i = 0; i < segments; i++) {
            double prevX = currentX;
            double prevY = currentY;
            double prevZ = currentZ;
            double deltaX = dirX * length * random.nextFloat();
            double deltaY = dirY * length * random.nextFloat();
            double deltaZ = dirZ * length * random.nextFloat();
            currentX += deltaX;
            currentY += deltaY;
            currentZ += deltaZ;
            float normalX = (float) deltaX;
            float normalY = (float) deltaY;
            float normalZ = (float) deltaZ;
            float normalLength = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
            if (normalLength <= 1.0E-6F) {
                normalY = 1.0F;
            } else {
                normalX /= normalLength;
                normalY /= normalLength;
                normalZ /= normalLength;
            }
            LegacyLineRenderer.lineWithNormal(outer, pose, normal, prevX, prevY, prevZ, currentX, currentY, currentZ,
                    colorOuter, 255, normalX, normalY, normalZ);
            LegacyLineRenderer.lineWithNormal(inner, pose, normal, prevX, prevY, prevZ, currentX, currentY, currentZ,
                    colorInner, 255, normalX, normalY, normalZ);
        }
    }

    public static SparkRenderPlan sparkPlan(int seed, double x, double y, double z,
            float length, int min, int max, int colorOuter, int colorInner) {
        Random random = LegacyRenderRandom.seeded(seed);
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
        Random random = LegacyRenderRandom.seeded(seed);
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
            currentX += dirX * length * scaleX;
            currentY += dirY * length * scaleY;
            currentZ += dirZ * length * scaleZ;
            result.add(new SparkSegment(prevX, prevY, prevZ, currentX, currentY, currentZ));
        }
        return result;
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
