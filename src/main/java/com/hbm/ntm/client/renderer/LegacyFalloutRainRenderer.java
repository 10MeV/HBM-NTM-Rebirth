package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Random;

public final class LegacyFalloutRainRenderer {
    public static final int COORD_GRID = 32;
    public static final int COORD_CENTER = 16;
    public static final int COORD_COUNT = COORD_GRID * COORD_GRID;
    public static final float RAIN_HALF_WIDTH = 0.5F;
    public static final float FALL_SPEED = 1.0F;
    public static final float FALL_VARIATION_BASE = 0.4F;
    public static final float FALL_VARIATION_SPAN = 0.2F;
    public static final float ALPHA_BASE = 0.5F;
    public static final float ALPHA_DISTANCE_SCALE = 0.3F;

    public static void fillRainCoords(float[] rainXCoords, float[] rainZCoords) {
        if (rainXCoords == null || rainZCoords == null) {
            return;
        }
        int count = Math.min(Math.min(rainXCoords.length, rainZCoords.length), COORD_COUNT);
        for (int index = 0; index < count; index++) {
            int i = index / COORD_GRID;
            int j = index % COORD_GRID;
            float x = j - COORD_CENTER;
            float z = i - COORD_CENTER;
            float length = Mth.sqrt(x * x + z * z);
            if (length <= 1.0E-6F) {
                rainXCoords[index] = 0.0F;
                rainZCoords[index] = 0.0F;
            } else {
                rainXCoords[index] = -z / length;
                rainZCoords[index] = x / length;
            }
        }
    }

    public static int rainCoordIndex(int layerX, int layerZ, int centerX, int centerZ) {
        return (layerZ - centerZ + COORD_CENTER) * COORD_GRID + layerX - centerX + COORD_CENTER;
    }

    public static float rainOffset(float[] coords, int index) {
        return coords == null || index < 0 || index >= coords.length ? 0.0F : coords[index] * RAIN_HALF_WIDTH;
    }

    public static long layerSeed(int layerX, int layerZ) {
        return layerX * layerX * 3121L + layerX * 45_238_971L ^ layerZ * layerZ * 418_711L + layerZ * 13_761L;
    }

    public static ColumnStyle columnStyle(Random random, int timer, float partialTick,
            double distX, double distZ, int renderLayerCount) {
        Random safeRandom = random == null ? new Random(0L) : random;
        float fallVariation = FALL_VARIATION_BASE + safeRandom.nextFloat() * FALL_VARIATION_SPAN;
        float swayVariation = safeRandom.nextFloat();
        float distanceMod = Mth.sqrt((float) (distX * distX + distZ * distZ)) / Math.max(1, renderLayerCount);
        float alpha = ((1.0F - distanceMod * distanceMod) * ALPHA_DISTANCE_SCALE + ALPHA_BASE);
        return new ColumnStyle(
                fallVariation,
                swayVariation,
                (float) LegacyUvAnimation.falloutRainSwayLoop(timer, partialTick),
                alpha);
    }

    public static HeightSpan heightSpan(int centerY, int renderLayerCount, int precipitationHeight) {
        int minHeight = Math.max(centerY - renderLayerCount, precipitationHeight);
        int maxHeight = Math.max(centerY + renderLayerCount, precipitationHeight);
        return new HeightSpan(minHeight, maxHeight);
    }

    public static int sampleLightY(int precipitationHeight, int cameraY) {
        return precipitationHeight < cameraY ? cameraY : precipitationHeight;
    }

    public static int blendLegacyLight(int packedLight) {
        return (packedLight * 3 + LightTexture.FULL_BRIGHT) / 4;
    }

    public static void renderColumn(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
            int layerX, int layerZ, HeightSpan height, float rainX, float rainZ,
            ColumnStyle style, int packedLight, double originX, double originY, double originZ) {
        if (consumer == null || height == null || style == null || height.minY() == height.maxY()) {
            return;
        }
        double u0 = LegacyUvAnimation.falloutRainU(0.0D, style.fallVariation(), FALL_SPEED);
        double u1 = LegacyUvAnimation.falloutRainU(1.0D, style.fallVariation(), FALL_SPEED);
        double minV = LegacyUvAnimation.falloutRainV(height.minY(), style.swayLoop(), style.swayVariation(), FALL_SPEED);
        double maxV = LegacyUvAnimation.falloutRainV(height.maxY(), style.swayLoop(), style.swayVariation(), FALL_SPEED);

        putVertex(consumer, pose, normal,
                layerX - rainX + 0.5D - originX, height.minY() - originY, layerZ - rainZ + 0.5D - originZ,
                u0, minV, style.alpha(), packedLight);
        putVertex(consumer, pose, normal,
                layerX + rainX + 0.5D - originX, height.minY() - originY, layerZ + rainZ + 0.5D - originZ,
                u1, minV, style.alpha(), packedLight);
        putVertex(consumer, pose, normal,
                layerX + rainX + 0.5D - originX, height.maxY() - originY, layerZ + rainZ + 0.5D - originZ,
                u1, maxV, style.alpha(), packedLight);
        putVertex(consumer, pose, normal,
                layerX - rainX + 0.5D - originX, height.maxY() - originY, layerZ - rainZ + 0.5D - originZ,
                u0, maxV, style.alpha(), packedLight);
    }

    public static void putVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
            double x, double y, double z, double u, double v, float alpha, int packedLight) {
        consumer.vertex(pose, (float) x, (float) y, (float) z)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv((float) u, (float) v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    public record HeightSpan(int minY, int maxY) {
    }

    public record ColumnStyle(float fallVariation, float swayVariation, float swayLoop, float alpha) {
    }

    private LegacyFalloutRainRenderer() {
    }
}
