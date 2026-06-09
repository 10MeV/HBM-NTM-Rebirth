package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Formula-only helpers for RenderNTMSkyboxChainloader and RenderNTMSkyboxImpact.
 */
public final class LegacySkyboxRenderer {
    public static final ResourceLocation SUN_TEXTURE =
            new ResourceLocation("minecraft", "textures/environment/sun.png");
    public static final ResourceLocation MOON_PHASES_TEXTURE =
            new ResourceLocation("minecraft", "textures/environment/moon_phases.png");
    public static final ResourceLocation DIGAMMA_STAR_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/misc/star_digamma.png");
    public static final ResourceLocation LODE_STAR_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/misc/star_lode.png");
    public static final ResourceLocation BOBMAZON_SAT_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/misc/sat_bobmazon.png");

    public static final double SKY_BODY_DISTANCE = 100.0D;
    public static final int STAR_SEED = 10842;
    public static final int STAR_ATTEMPTS = 1500;
    public static final float SKY_GRID_Y = 16.0F;
    public static final int SKY_GRID_STEP = 64;

    public static float impactDust(float atmosphericDust) {
        return Math.max(1.0F - atmosphericDust * 2.0F, 0.0F);
    }

    public static float impactRain(float atmosphericDust, float rainStrength) {
        return impactDust(atmosphericDust) * (1.0F - rainStrength);
    }

    public static float celestialBrightness(float celestialAngle) {
        float brightness = (float) Math.sin(celestialAngle * Math.PI);
        return brightness * brightness;
    }

    public static SkyBodyPlan lodeStarPlan(float randomUnit) {
        float size = 0.5F + randomUnit * 0.25F;
        return new SkyBodyPlan(LODE_STAR_TEXTURE, size, SKY_BODY_DISTANCE, 1.0F,
                List.of(new Rotation(-75.0F, 1.0F, 0.0F, 0.0F),
                        new Rotation(10.0F, 0.0F, 1.0F, 0.0F)));
    }

    public static SkyBodyPlan digammaStarPlan(float celestialAngle, float digamma, float alpha) {
        float size = 1.0F + digamma * 0.25F;
        double distance = SKY_BODY_DISTANCE - digamma * 2.5D;
        return new SkyBodyPlan(DIGAMMA_STAR_TEXTURE, size, distance, alpha,
                List.of(new Rotation(-90.0F, 0.0F, 1.0F, 0.0F),
                        new Rotation(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F),
                        new Rotation(140.0F, 1.0F, 0.0F, 0.0F),
                        new Rotation(-40.0F, 0.0F, 0.0F, 1.0F)));
    }

    public static SkyBodyPlan bobmazonSatellitePlan(long currentMillis, float alpha) {
        return new SkyBodyPlan(BOBMAZON_SAT_TEXTURE, 0.5F, SKY_BODY_DISTANCE, alpha,
                List.of(new Rotation(-40.0F, 1.0F, 0.0F, 0.0F),
                        new Rotation((currentMillis % (360L * 1000L)) / 1000.0F, 0.0F, 1.0F, 0.0F),
                        new Rotation((currentMillis % (360L * 100L)) / 100.0F, 1.0F, 0.0F, 0.0F)));
    }

    public static SkyBodyPlan sunPlan(float rainAlpha) {
        return new SkyBodyPlan(SUN_TEXTURE, 30.0F, SKY_BODY_DISTANCE, rainAlpha, List.of());
    }

    public static SkyBodyPlan moonPlan(int moonPhase, float rainAlpha) {
        return new SkyBodyPlan(MOON_PHASES_TEXTURE, 20.0F, -SKY_BODY_DISTANCE, rainAlpha, List.of());
    }

    public static MoonUv moonUv(int moonPhase) {
        int column = moonPhase % 4;
        int row = moonPhase / 4 % 2;
        return new MoonUv((column + 0) / 4.0F, (row + 0) / 2.0F,
                (column + 1) / 4.0F, (row + 1) / 2.0F);
    }

    public static List<SkyVertex> bodyQuad(double size, double distance) {
        return List.of(
                new SkyVertex(-size, distance, -size, 0.0D, 0.0D),
                new SkyVertex(size, distance, -size, 0.0D, 1.0D),
                new SkyVertex(size, distance, size, 1.0D, 1.0D),
                new SkyVertex(-size, distance, size, 1.0D, 0.0D));
    }

    public static List<SkyQuad> starQuads() {
        return starQuads(STAR_SEED, STAR_ATTEMPTS);
    }

    public static List<SkyQuad> starQuads(long seed, int attempts) {
        Random random = new Random(seed);
        List<SkyQuad> quads = new ArrayList<>();
        for (int i = 0; i < attempts; ++i) {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 < 1.0D && d4 > 0.01D) {
                double inv = 1.0D / Math.sqrt(d4);
                d0 *= inv;
                d1 *= inv;
                d2 *= inv;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);
                List<SkyVertex> vertices = new ArrayList<>(4);
                for (int j = 0; j < 4; ++j) {
                    double d17 = 0.0D;
                    double d18 = ((j & 2) - 1) * d3;
                    double d19 = ((j + 1 & 2) - 1) * d3;
                    double d20 = d18 * d16 - d19 * d15;
                    double d21 = d19 * d16 + d18 * d15;
                    double d22 = d20 * d12 + d17 * d13;
                    double d23 = d17 * d12 - d20 * d13;
                    double d24 = d23 * d9 - d21 * d10;
                    double d25 = d21 * d9 + d23 * d10;
                    vertices.add(new SkyVertex(d5 + d24, d6 + d22, d7 + d25, 0.0D, 0.0D));
                }
                quads.add(new SkyQuad(vertices));
            }
        }
        return List.copyOf(quads);
    }

    public static HorizonMaskPlan horizonMask(double playerY, double horizon) {
        double d0 = playerY - horizon;
        if (d0 >= 0.0D) {
            return HorizonMaskPlan.hidden();
        }
        return new HorizonMaskPlan(true, 12.0F, 1.0F, -((float) (d0 + 65.0D)), -1.0F);
    }

    public record Rotation(float angleDegrees, float axisX, float axisY, float axisZ) {
    }

    public record SkyBodyPlan(ResourceLocation texture, float halfSize, double distance, float alpha,
            List<Rotation> rotations) {
        public List<SkyVertex> quad() {
            return bodyQuad(halfSize, distance);
        }
    }

    public record MoonUv(float u0, float v0, float u1, float v1) {
    }

    public record SkyVertex(double x, double y, double z, double u, double v) {
    }

    public record SkyQuad(List<SkyVertex> vertices) {
    }

    public record HorizonMaskPlan(boolean visible, float skyListTranslateY, float halfWidth, float topY, float bottomY) {
        public static HorizonMaskPlan hidden() {
            return new HorizonMaskPlan(false, 0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    private LegacySkyboxRenderer() {
    }
}
