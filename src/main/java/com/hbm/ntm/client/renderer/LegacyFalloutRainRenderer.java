package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.render.LegacyRenderRandom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

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
        Random safeRandom = random == null ? LegacyRenderRandom.seeded(0L) : random;
        return new ColumnStyle(fallVariation(safeRandom), swayVariation(safeRandom), swayLoop(timer, partialTick),
                alpha(distX, distZ, renderLayerCount));
    }

    public static HeightSpan heightSpan(int centerY, int renderLayerCount, int precipitationHeight) {
        return new HeightSpan(minHeight(centerY, renderLayerCount, precipitationHeight),
                maxHeight(centerY, renderLayerCount, precipitationHeight));
    }

    public static int minHeight(int centerY, int renderLayerCount, int precipitationHeight) {
        return Math.max(centerY - renderLayerCount, precipitationHeight);
    }

    public static int maxHeight(int centerY, int renderLayerCount, int precipitationHeight) {
        return Math.max(centerY + renderLayerCount, precipitationHeight);
    }

    public static float fallVariation(Random random) {
        Random safeRandom = random == null ? LegacyRenderRandom.seeded(0L) : random;
        return FALL_VARIATION_BASE + safeRandom.nextFloat() * FALL_VARIATION_SPAN;
    }

    public static float swayVariation(Random random) {
        Random safeRandom = random == null ? LegacyRenderRandom.seeded(0L) : random;
        return safeRandom.nextFloat();
    }

    public static float swayLoop(int timer, float partialTick) {
        return (float) LegacyUvAnimation.falloutRainSwayLoop(timer, partialTick);
    }

    public static float alpha(double distX, double distZ, int renderLayerCount) {
        double range = Math.max(1, renderLayerCount);
        double distanceModSqr = (distX * distX + distZ * distZ) / (range * range);
        return (float) ((1.0D - distanceModSqr) * ALPHA_DISTANCE_SCALE + ALPHA_BASE);
    }

    public static int sampleLightY(int precipitationHeight, int cameraY) {
        return precipitationHeight < cameraY ? cameraY : precipitationHeight;
    }

    public static int blendLegacyLight(int packedLight) {
        return (packedLight * 3 + LightTexture.FULL_BRIGHT) / 4;
    }

    public static void renderColumn(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int layerX, int layerZ, HeightSpan height, float rainX, float rainZ,
            ColumnStyle style, int packedLight, double originX, double originY, double originZ) {
        if (texture == null || poseStack == null || buffer == null || height == null || style == null
                || height.minY() == height.maxY()) {
            return;
        }
        renderColumn(texture, poseStack, buffer, layerX, layerZ, height.minY(), height.maxY(), rainX, rainZ,
                style.fallVariation(), style.swayVariation(), style.swayLoop(), style.alpha(), packedLight,
                originX, originY, originZ);
    }

    public static void renderColumn(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int layerX, int layerZ, int minHeight, int maxHeight, float rainX, float rainZ,
            float fallVariation, float swayVariation, float swayLoop, float alpha,
            int packedLight, double originX, double originY, double originZ) {
        if (texture == null || poseStack == null || buffer == null || minHeight == maxHeight) {
            return;
        }
        VertexConsumer consumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(texture, buffer,
                LegacyTexturedRenderMode.TRANSLUCENT);
        renderColumn(consumer, poseStack.last(), layerX, layerZ, minHeight, maxHeight, rainX, rainZ,
                fallVariation, swayVariation, swayLoop, alpha, packedLight, originX, originY, originZ);
    }

    public static void renderColumn(VertexConsumer consumer, PoseStack.Pose pose,
            int layerX, int layerZ, int minHeight, int maxHeight, float rainX, float rainZ,
            float fallVariation, float swayVariation, float swayLoop, float alpha,
            int packedLight, double originX, double originY, double originZ) {
        if (consumer == null || pose == null || minHeight == maxHeight) {
            return;
        }
        double u0 = LegacyUvAnimation.falloutRainU(0.0D, fallVariation, FALL_SPEED);
        double u1 = LegacyUvAnimation.falloutRainU(1.0D, fallVariation, FALL_SPEED);
        double minV = LegacyUvAnimation.falloutRainV(minHeight, swayLoop, swayVariation, FALL_SPEED);
        double maxV = LegacyUvAnimation.falloutRainV(maxHeight, swayLoop, swayVariation, FALL_SPEED);
        int alphaInt = LegacyTexturedQuadRenderer.alpha(alpha);
        double x0 = layerX - rainX + 0.5D - originX;
        double z0 = layerZ - rainZ + 0.5D - originZ;
        double x1 = layerX + rainX + 0.5D - originX;
        double z1 = layerZ + rainZ + 0.5D - originZ;
        double minY = minHeight - originY;
        double maxY = maxHeight - originY;

        LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, packedLight, OverlayTexture.NO_OVERLAY,
                0.0F, 1.0F, 0.0F,
                x0, minY, z0, u0, minV, alphaInt,
                x1, minY, z1, u1, minV, alphaInt,
                x1, maxY, z1, u1, maxV, alphaInt,
                x0, maxY, z0, u0, maxV, alphaInt,
                0xFFFFFF);
    }

    public record HeightSpan(int minY, int maxY) {
    }

    public record ColumnStyle(float fallVariation, float swayVariation, float swayLoop, float alpha) {
    }

    private LegacyFalloutRainRenderer() {
    }
}
