package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable plan helpers for the 1.7.10 RenderEmitter beam stack.
 */
public final class LegacyEmitterBeamRenderer {
    public static final float INNER_COLOR_MULTIPLIER = 0.85F;
    public static final float OUTER_COLOR_MULTIPLIER = 0.1F;
    public static final double MODEL_CENTER_X = 0.5D;
    public static final double MODEL_CENTER_Z = 0.5D;
    public static final float BASE_YAW_DEGREES = 90.0F;
    public static final double FINAL_BEAM_OFFSET_Y = 0.5D;
    public static final double FINAL_BEAM_OFFSET_Z = 0.5D;

    public static int range(int beam) {
        return beam - 1;
    }

    public static int emitterColor(long worldTime, int rawColor) {
        if (rawColor != 0) {
            return rawColor & 0xFFFFFF;
        }
        return Color.HSBtoRGB(worldTime / 50.0F, 0.5F, 0.25F) & 0xFFFFFF;
    }

    public static BeamColors colors(long worldTime, int rawColor) {
        int color = emitterColor(worldTime, rawColor);
        return new BeamColors(color, multiply(color, INNER_COLOR_MULTIPLIER), multiply(color, OUTER_COLOR_MULTIPLIER));
    }

    public static int multiply(int color, float multiplier) {
        int red = (int) (((color >> 16) & 255) * multiplier);
        int green = (int) (((color >> 8) & 255) * multiplier);
        int blue = (int) ((color & 255) * multiplier);
        return red << 16 | green << 8 | blue;
    }

    public static int baseLayerCount(float girth) {
        return (int) Math.max(Math.sqrt(girth * 50.0F), 2.0D);
    }

    public static int effectSegments(int range, float girth, int divisor) {
        if (girth <= 0.0F) {
            return 1;
        }
        return (int) Math.max(range / girth / divisor, 1.0F);
    }

    public static EmitterTransform transformForMetadata(int metadata) {
        return switch (metadata) {
            case 0 -> new EmitterTransform(0.0D, 0.5D, -0.5D, 90.0F, 1.0F, 0.0F, 0.0F);
            case 1 -> new EmitterTransform(0.0D, 0.5D, 0.5D, 90.0F, -1.0F, 0.0F, 0.0F);
            case 2 -> new EmitterTransform(0.0D, 0.0D, 0.0D, 90.0F, 0.0F, 1.0F, 0.0F);
            case 3 -> new EmitterTransform(0.0D, 0.0D, 0.0D, 270.0F, 0.0F, 1.0F, 0.0F);
            case 4 -> new EmitterTransform(0.0D, 0.0D, 0.0D, 180.0F, 0.0F, 1.0F, 0.0F);
            default -> new EmitterTransform(0.0D, 0.0D, 0.0D, 0.0F, 0.0F, 1.0F, 0.0F);
        };
    }

    public static List<EmitterBeamPlan> beamPlans(int beam, float girth, int effect, int rawColor,
            long worldTime, float partialTick) {
        int range = range(beam);
        if (range <= 0) {
            return List.of();
        }
        BeamColors colors = colors(worldTime, rawColor);
        List<EmitterBeamPlan> plans = new ArrayList<>();
        plans.add(baseBeamPlan(range, girth, colors));
        plans.addAll(effectBeamPlans(effect, range, girth, colors, worldTime, partialTick));
        return List.copyOf(plans);
    }

    public static EmitterBeamPlan baseBeamPlan(int range, float girth, BeamColors colors) {
        return new EmitterBeamPlan(0.0D, 0.0D, range,
                LegacyBeamRenderer.WaveType.SPIRAL, colors.outerColor(), colors.innerColor(),
                0, 1, 0.0F, baseLayerCount(girth), girth);
    }

    public static List<EmitterBeamPlan> effectBeamPlans(int effect, int range, float girth,
            BeamColors colors, long worldTime, float partialTick) {
        if (effect <= 0) {
            return List.of();
        }
        int longSegments = effectSegments(range, girth, 2);
        float size = girth * 2.0F;
        float thickness = girth * 0.1F;
        List<EmitterBeamPlan> plans = new ArrayList<>();
        if (effect == 1) {
            int start = (int) worldTime / 2;
            plans.add(effectPlan(range, LegacyBeamRenderer.WaveType.RANDOM, colors, start,
                    longSegments, size, thickness));
            plans.add(effectPlan(range, LegacyBeamRenderer.WaveType.RANDOM, colors, start + 15,
                    effectSegments(range, girth, 4), size, thickness));
            return List.copyOf(plans);
        }

        int spinStart = (int) (worldTime + partialTick) * -10 % 360;
        plans.add(effectPlan(range, LegacyBeamRenderer.WaveType.SPIRAL, colors, spinStart,
                longSegments, size, thickness));
        if (effect == 2) {
            plans.add(effectPlan(range, LegacyBeamRenderer.WaveType.SPIRAL, colors, spinStart + 180,
                    longSegments, size, thickness));
        } else if (effect == 3) {
            plans.add(effectPlan(range, LegacyBeamRenderer.WaveType.SPIRAL, colors, spinStart + 120,
                    longSegments, size, thickness));
            plans.add(effectPlan(range, LegacyBeamRenderer.WaveType.SPIRAL, colors, spinStart + 240,
                    longSegments, size, thickness));
        }
        return List.copyOf(plans);
    }

    private static EmitterBeamPlan effectPlan(int range, LegacyBeamRenderer.WaveType wave, BeamColors colors,
            int start, int segments, float size, float thickness) {
        return new EmitterBeamPlan(0.0D, 0.0D, range, wave, colors.outerColor(), colors.innerColor(),
                start, segments, size, 4, thickness);
    }

    public static void renderPlan(EmitterBeamPlan plan, PoseStack poseStack, MultiBufferSource buffer) {
        LegacyBeamRenderer.solidBeam(poseStack, buffer, plan.x(), plan.y(), plan.z(),
                plan.wave(), plan.outerColor(), plan.innerColor(), plan.start(), plan.segments(),
                plan.size(), plan.layers(), plan.thickness());
    }

    public static void renderPlans(List<EmitterBeamPlan> plans, PoseStack poseStack, MultiBufferSource buffer) {
        for (EmitterBeamPlan plan : plans) {
            renderPlan(plan, poseStack, buffer);
        }
    }

    public record BeamColors(int originalColor, int innerColor, int outerColor) {
    }

    public record EmitterTransform(double translateX, double translateY, double translateZ,
            float angleDegrees, float axisX, float axisY, float axisZ) {
        public boolean hasRotation() {
            return angleDegrees != 0.0F && (axisX != 0.0F || axisY != 0.0F || axisZ != 0.0F);
        }
    }

    public record EmitterBeamPlan(double x, double y, double z, LegacyBeamRenderer.WaveType wave,
            int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
    }

    private LegacyEmitterBeamRenderer() {
    }
}
