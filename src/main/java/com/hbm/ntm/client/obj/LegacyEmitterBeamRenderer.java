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
    private static final EmitterTransform DEFAULT_TRANSFORM =
            new EmitterTransform(0.0D, 0.0D, 0.0D, 0.0F, 0.0F, 1.0F, 0.0F);
    private static final EmitterTransform[] METADATA_TRANSFORMS = {
            new EmitterTransform(0.0D, 0.5D, -0.5D, 90.0F, 1.0F, 0.0F, 0.0F),
            new EmitterTransform(0.0D, 0.5D, 0.5D, 90.0F, -1.0F, 0.0F, 0.0F),
            new EmitterTransform(0.0D, 0.0D, 0.0D, 90.0F, 0.0F, 1.0F, 0.0F),
            new EmitterTransform(0.0D, 0.0D, 0.0D, 270.0F, 0.0F, 1.0F, 0.0F),
            new EmitterTransform(0.0D, 0.0D, 0.0D, 180.0F, 0.0F, 1.0F, 0.0F),
            DEFAULT_TRANSFORM
    };

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
        return metadata >= 0 && metadata < METADATA_TRANSFORMS.length
                ? METADATA_TRANSFORMS[metadata]
                : DEFAULT_TRANSFORM;
    }

    public static List<EmitterBeamPlan> beamPlans(int beam, float girth, int effect, int rawColor,
            long worldTime, float partialTick) {
        List<EmitterBeamPlan> plans = new ArrayList<>();
        emitBeamPlans(beam, girth, effect, rawColor, worldTime, partialTick,
                (x, y, z, wave, outerColor, innerColor, start, segments, size, layers, thickness) ->
                        plans.add(new EmitterBeamPlan(x, y, z, wave, outerColor, innerColor,
                                start, segments, size, layers, thickness)));
        return List.copyOf(plans);
    }

    public static void emitBeamPlans(int beam, float girth, int effect, int rawColor,
            long worldTime, float partialTick, EmitterBeamPlanSink sink) {
        if (sink == null) {
            return;
        }
        int range = range(beam);
        if (range <= 0) {
            return;
        }
        int originalColor = emitterColor(worldTime, rawColor);
        int innerColor = multiply(originalColor, INNER_COLOR_MULTIPLIER);
        int outerColor = multiply(originalColor, OUTER_COLOR_MULTIPLIER);
        emitBaseBeamPlan(sink, range, girth, outerColor, innerColor);
        emitEffectBeamPlans(sink, effect, range, girth, outerColor, innerColor, worldTime, partialTick);
    }

    public static EmitterBeamPlan baseBeamPlan(int range, float girth, BeamColors colors) {
        return new EmitterBeamPlan(0.0D, 0.0D, range,
                LegacyBeamRenderer.WaveType.SPIRAL, colors.outerColor(), colors.innerColor(),
                0, 1, 0.0F, baseLayerCount(girth), girth);
    }

    private static void emitBaseBeamPlan(EmitterBeamPlanSink sink, int range, float girth,
            int outerColor, int innerColor) {
        sink.add(0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.SPIRAL,
                outerColor, innerColor, 0, 1, 0.0F, baseLayerCount(girth), girth);
    }

    public static List<EmitterBeamPlan> effectBeamPlans(int effect, int range, float girth,
            BeamColors colors, long worldTime, float partialTick) {
        if (effect <= 0 || effect > 3) {
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

    private static void emitEffectBeamPlans(EmitterBeamPlanSink sink, int effect, int range, float girth,
            int outerColor, int innerColor, long worldTime, float partialTick) {
        if (effect <= 0 || effect > 3) {
            return;
        }
        int longSegments = effectSegments(range, girth, 2);
        float size = girth * 2.0F;
        float thickness = girth * 0.1F;
        if (effect == 1) {
            int start = (int) worldTime / 2;
            emitEffectPlan(sink, range, LegacyBeamRenderer.WaveType.RANDOM, outerColor, innerColor, start,
                    longSegments, size, thickness);
            emitEffectPlan(sink, range, LegacyBeamRenderer.WaveType.RANDOM, outerColor, innerColor, start + 15,
                    effectSegments(range, girth, 4), size, thickness);
            return;
        }

        int spinStart = (int) (worldTime + partialTick) * -10 % 360;
        emitEffectPlan(sink, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor, spinStart,
                longSegments, size, thickness);
        if (effect == 2) {
            emitEffectPlan(sink, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor, spinStart + 180,
                    longSegments, size, thickness);
        } else if (effect == 3) {
            emitEffectPlan(sink, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor, spinStart + 120,
                    longSegments, size, thickness);
            emitEffectPlan(sink, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor, spinStart + 240,
                    longSegments, size, thickness);
        }
    }

    public static void renderDirect(int beam, float girth, int effect, int rawColor,
            long worldTime, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        int range = range(beam);
        if (range <= 0) {
            return;
        }
        int originalColor = emitterColor(worldTime, rawColor);
        int innerColor = multiply(originalColor, INNER_COLOR_MULTIPLIER);
        int outerColor = multiply(originalColor, OUTER_COLOR_MULTIPLIER);
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        renderBaseBeamDirect(beamBatch, range, girth, outerColor, innerColor);
        renderEffectBeamsDirect(beamBatch, effect, range, girth, outerColor, innerColor, worldTime, partialTick);
    }

    public static void renderBaseBeamDirect(int range, float girth, BeamColors colors,
            PoseStack poseStack, MultiBufferSource buffer) {
        LegacyBeamRenderer.solidBeam(poseStack, buffer, 0.0D, 0.0D, range,
                LegacyBeamRenderer.WaveType.SPIRAL, colors.outerColor(), colors.innerColor(),
                0, 1, 0.0F, baseLayerCount(girth), girth);
    }

    private static void renderBaseBeamDirect(LegacyBeamRenderer.DirectSolidBeamBatch beamBatch,
            int range, float girth, BeamColors colors) {
        renderBaseBeamDirect(beamBatch, range, girth, colors.outerColor(), colors.innerColor());
    }

    private static void renderBaseBeamDirect(LegacyBeamRenderer.DirectSolidBeamBatch beamBatch,
            int range, float girth, int outerColor, int innerColor) {
        LegacyBeamRenderer.solidBeam(beamBatch, 0.0D, 0.0D, range,
                LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor,
                0, 1, 0.0F, baseLayerCount(girth), girth);
    }

    public static void renderEffectBeamsDirect(int effect, int range, float girth,
            BeamColors colors, long worldTime, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        if (effect <= 0 || effect > 3) {
            return;
        }
        renderEffectBeamsDirect(LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false),
                effect, range, girth, colors.outerColor(), colors.innerColor(), worldTime, partialTick);
    }

    private static void renderEffectBeamsDirect(LegacyBeamRenderer.DirectSolidBeamBatch beamBatch,
            int effect, int range, float girth, BeamColors colors, long worldTime, float partialTick) {
        renderEffectBeamsDirect(beamBatch, effect, range, girth,
                colors.outerColor(), colors.innerColor(), worldTime, partialTick);
    }

    private static void renderEffectBeamsDirect(LegacyBeamRenderer.DirectSolidBeamBatch beamBatch,
            int effect, int range, float girth, int outerColor, int innerColor,
            long worldTime, float partialTick) {
        if (effect <= 0 || effect > 3) {
            return;
        }
        int longSegments = effectSegments(range, girth, 2);
        float size = girth * 2.0F;
        float thickness = girth * 0.1F;
        if (effect == 1) {
            int start = (int) worldTime / 2;
            renderEffectBeamDirect(beamBatch, range, LegacyBeamRenderer.WaveType.RANDOM, outerColor, innerColor, start,
                    longSegments, size, thickness);
            renderEffectBeamDirect(beamBatch, range, LegacyBeamRenderer.WaveType.RANDOM, outerColor, innerColor,
                    start + 15,
                    effectSegments(range, girth, 4), size, thickness);
            return;
        }

        int spinStart = (int) (worldTime + partialTick) * -10 % 360;
        renderEffectBeamDirect(beamBatch, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor, spinStart,
                longSegments, size, thickness);
        if (effect == 2) {
            renderEffectBeamDirect(beamBatch, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor,
                    spinStart + 180,
                    longSegments, size, thickness);
        } else {
            renderEffectBeamDirect(beamBatch, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor,
                    spinStart + 120,
                    longSegments, size, thickness);
            renderEffectBeamDirect(beamBatch, range, LegacyBeamRenderer.WaveType.SPIRAL, outerColor, innerColor,
                    spinStart + 240,
                    longSegments, size, thickness);
        }
    }

    private static void renderEffectBeamDirect(LegacyBeamRenderer.DirectSolidBeamBatch beamBatch,
            int range, LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
            int start, int segments, float size, float thickness) {
        LegacyBeamRenderer.solidBeam(beamBatch, 0.0D, 0.0D, range, wave,
                outerColor, innerColor, start, segments, size, 4, thickness);
    }

    private static EmitterBeamPlan effectPlan(int range, LegacyBeamRenderer.WaveType wave, BeamColors colors,
            int start, int segments, float size, float thickness) {
        return new EmitterBeamPlan(0.0D, 0.0D, range, wave, colors.outerColor(), colors.innerColor(),
                start, segments, size, 4, thickness);
    }

    private static void emitEffectPlan(EmitterBeamPlanSink sink, int range, LegacyBeamRenderer.WaveType wave,
            int outerColor, int innerColor, int start, int segments, float size, float thickness) {
        sink.add(0.0D, 0.0D, range, wave, outerColor, innerColor,
                start, segments, size, 4, thickness);
    }

    public static void renderPlan(EmitterBeamPlan plan, PoseStack poseStack, MultiBufferSource buffer) {
        LegacyBeamRenderer.solidBeam(poseStack, buffer, plan.x(), plan.y(), plan.z(),
                plan.wave(), plan.outerColor(), plan.innerColor(), plan.start(), plan.segments(),
                plan.size(), plan.layers(), plan.thickness());
    }

    public static void renderPlans(List<EmitterBeamPlan> plans, PoseStack poseStack, MultiBufferSource buffer) {
        if (plans.isEmpty()) {
            return;
        }
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        for (EmitterBeamPlan plan : plans) {
            LegacyBeamRenderer.solidBeam(beamBatch, plan.x(), plan.y(), plan.z(),
                    plan.wave(), plan.outerColor(), plan.innerColor(), plan.start(), plan.segments(),
                    plan.size(), plan.layers(), plan.thickness());
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

    @FunctionalInterface
    public interface EmitterBeamPlanSink {
        void add(double x, double y, double z, LegacyBeamRenderer.WaveType wave,
                int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness);
    }

    private LegacyEmitterBeamRenderer() {
    }
}
