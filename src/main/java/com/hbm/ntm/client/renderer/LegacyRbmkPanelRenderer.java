package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.obj.LegacyRenderColor;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.hbm.ntm.util.HbmMathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class LegacyRbmkPanelRenderer {
    private static final LegacyWavefrontModel.SelectionHandle GAUGE_BODY =
            ObjRbmkModels.GAUGE.prepareRenderOnlyInCallOrder("Gauge");
    private static final LegacyWavefrontModel.SelectionHandle GAUGE_NEEDLE =
            ObjRbmkModels.GAUGE.prepareRenderOnlyInCallOrder("Needle");
    private static final LegacyWavefrontModel.SelectionHandle INDICATOR_BASE =
            ObjRbmkModels.INDICATOR.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle INDICATOR_LIGHT =
            ObjRbmkModels.INDICATOR.prepareRenderOnlyInCallOrder("Light");
    private static final LegacyWavefrontModel.SelectionHandle LEVER_BASE =
            ObjRbmkModels.LEVER.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle LEVER_HANDLE =
            ObjRbmkModels.LEVER.prepareRenderOnlyInCallOrder("Lever");
    private static final LegacyWavefrontModel.SelectionHandle KEY_SOCKET =
            ObjRbmkModels.BUTTON.prepareRenderOnlyInCallOrder("Socket");
    private static final LegacyWavefrontModel.SelectionHandle KEY_BUTTON =
            ObjRbmkModels.BUTTON.prepareRenderOnlyInCallOrder("Button");

    public static final int GAUGE_COUNT = 4;
    public static final int GRAPH_COUNT = 2;
    public static final int INDICATOR_COUNT = 6;
    public static final int KEY_COUNT = 4;
    public static final int LEVER_COUNT = 2;
    public static final int NUMITRON_COUNT = 2;

    public static final double PANEL_X = 0.25D;
    public static final double GAUGE_ROW_STEP = -0.5D;
    public static final double GAUGE_COLUMN_STEP = -0.5D;
    public static final double GAUGE_Y_START = 0.25D;
    public static final double GAUGE_Z_START = 0.25D;
    public static final double GAUGE_PIVOT_Y = 0.4375D;
    public static final double GAUGE_PIVOT_Z = -0.125D;
    public static final double GAUGE_MIN_MARK_ANGLE = 10.0D;
    public static final double GAUGE_MAX_MARK_ANGLE = 60.0D;
    public static final double GAUGE_NEEDLE_BASE_ANGLE = -85.0D;
    public static final double GAUGE_NEEDLE_SPAN = 50.0D;
    public static final double GAUGE_NEEDLE_MAX = 80.0D;
    public static final double GAUGE_LABEL_X = 0.01D;
    public static final double GAUGE_LABEL_Y = 0.3125D;
    public static final double GAUGE_LIMIT_LABEL_X = 0.032D;
    public static final double GAUGE_LIMIT_LABEL_Y = 0.4375D;
    public static final double GAUGE_LIMIT_LABEL_Z = 0.125D;
    public static final double GAUGE_LIMIT_TEXT_SCALE = 0.0025D;
    public static final double GAUGE_LABEL_MAX_WIDTH = 0.4D;

    public static final double INDICATOR_ROW_STEP = -0.3125D;
    public static final double INDICATOR_COLUMN_STEP = -0.5D;
    public static final double INDICATOR_Y_START = 0.3125D;
    public static final double INDICATOR_Z_START = 0.25D;
    public static final double INDICATOR_LABEL_X = 0.0725D;
    public static final double INDICATOR_LABEL_Y = 0.5D;
    public static final double INDICATOR_LABEL_MAX_WIDTH = 0.3D;
    public static final float INDICATOR_DIM_MULTIPLIER = 0.35F;

    public static final double KEY_ROW_STEP = -0.5D;
    public static final double KEY_COLUMN_STEP = -0.5D;
    public static final double KEY_Y_START = 0.25D;
    public static final double KEY_Z_START = 0.25D;
    public static final double KEY_PRESSED_X_OFFSET = -0.03125D;
    public static final double KEY_LABEL_X = 0.01D;
    public static final double KEY_LABEL_Y = 0.3125D;
    public static final double KEY_LABEL_MAX_WIDTH = 0.4D;
    public static final float KEY_DIM_MULTIPLIER = 0.65F;

    public static final double LEVER_Z_START = 0.25D;
    public static final double LEVER_COLUMN_STEP = -0.5D;
    public static final double LEVER_PIVOT_X = 0.125D;
    public static final double LEVER_PIVOT_Y = 0.5625D;
    public static final double LEVER_LABEL_X = 0.01D;
    public static final double LEVER_LABEL_Y = 0.0625D;
    public static final double LEVER_LABEL_MAX_WIDTH = 0.4D;

    public static final double NUMITRON_ROW_STEP = -0.5D;
    public static final double NUMITRON_Y_START = 0.25D;
    public static final double NUMITRON_LABEL_X = 0.01D;
    public static final double NUMITRON_LABEL_Y = 0.3125D;
    public static final double NUMITRON_LABEL_MAX_WIDTH = 0.75D;
    public static final double NUMITRON_DIGIT_X = 0.03135D;
    public static final double NUMITRON_DIGIT_Y = 0.5625D;
    public static final double NUMITRON_DIGIT_Z_STEP = 0.1D;
    public static final double NUMITRON_DIGIT_SCALE = 200.0D;
    public static final double NUMITRON_DIGIT_W = 8.0D / NUMITRON_DIGIT_SCALE;
    public static final double NUMITRON_DIGIT_H = 13.0D / NUMITRON_DIGIT_SCALE;
    public static final int NUMITRON_DIGITS = 7;
    public static final long NUMITRON_LEFT_DIGIT_MASK = 0x40L;

    public static final double GRAPH_ROW_STEP = -0.5D;
    public static final double GRAPH_Y_START = 0.25D;
    public static final double GRAPH_LINE_X = 0.03225D;
    public static final double GRAPH_BASE_Y = 0.5D - 0.03125D;
    public static final double GRAPH_HEIGHT = 0.1875D;
    public static final double GRAPH_Z_START = 0.375D;
    public static final double GRAPH_Z_SPAN = 0.75D;
    public static final double GRAPH_LIMIT_LABEL_X = 0.032D;
    public static final double GRAPH_LIMIT_LABEL_Y = 0.5D - 0.03125D * 1.5D;
    public static final double GRAPH_LIMIT_LABEL_Z = -0.375D + 0.03125D;
    public static final double GRAPH_LIMIT_LABEL_STEP_Y = -0.03125D * 7.0D;
    public static final double GRAPH_LIMIT_TEXT_SCALE = 0.0025D;
    public static final double GRAPH_LABEL_X = 0.01D;
    public static final double GRAPH_LABEL_Y = 0.3125D;
    public static final double GRAPH_LABEL_MAX_WIDTH = 0.75D;

    public static void renderGauges(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, RBMKPanelPlanner.GaugeUnit[] gauges, float partialTick) {
        if (gauges == null) {
            return;
        }
        int count = Math.min(GAUGE_COUNT, gauges.length);
        for (int i = 0; i < count; i++) {
            RBMKPanelPlanner.GaugeUnit unit = gauges[i];
            if (unit == null || !unit.active()) {
                continue;
            }
            renderGauge(poseStack, buffer, packedLight, packedOverlay, unit, i, partialTick);
        }
    }

    public static void renderGauge(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            RBMKPanelPlanner.GaugeUnit unit, int index, float partialTick) {
        poseStack.pushPose();
        translateGaugeSlot(poseStack, index);
        renderPreparedTexturedSelection(ObjRbmkModels.GAUGE, ObjRbmkModels.GAUGE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, 0xFFFFFF, GAUGE_BODY);

        int color = unit == null ? 0xFFFFFF : unit.color();
        double angle = gaugeNeedleAngle(unit, partialTick);
        poseStack.pushPose();
        poseStack.translate(0.0D, GAUGE_PIVOT_Y, GAUGE_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) -angle));
        poseStack.translate(0.0D, -GAUGE_PIVOT_Y, -GAUGE_PIVOT_Z);
        renderPreparedTexturedSelection(ObjRbmkModels.GAUGE, ObjRbmkModels.GAUGE_TEXTURE,
                poseStack, buffer, LightTexture.FULL_BRIGHT, packedOverlay, color, GAUGE_NEEDLE);
        poseStack.popPose();
        renderGaugeLimitLabels(poseStack, buffer, packedLight, unit);
        renderCenteredLegacyText(poseStack, buffer, packedLight, unit == null ? "" : unit.label(),
                GAUGE_LABEL_X, GAUGE_LABEL_Y, 0.0D, GAUGE_LABEL_MAX_WIDTH, 0x00FF00, true);
        poseStack.popPose();
    }

    public static double gaugeNeedleAngle(RBMKPanelPlanner.GaugeUnit unit, float partialTick) {
        if (unit == null) {
            return GAUGE_NEEDLE_BASE_ANGLE;
        }
        double value = unit.lastRenderValue() + (unit.renderValue() - unit.lastRenderValue()) * partialTick;
        long lower = Math.min(unit.min(), unit.max());
        long upper = Math.max(unit.min(), unit.max());
        if (lower == upper) {
            upper += 1L;
        }
        double angle = (value - lower) / (double) (upper - lower) * GAUGE_NEEDLE_SPAN;
        if (unit.min() > unit.max()) {
            angle = GAUGE_NEEDLE_SPAN - angle;
        }
        return Mth.clamp(angle, 0.0D, GAUGE_NEEDLE_MAX) + GAUGE_NEEDLE_BASE_ANGLE;
    }

    public static void renderIndicators(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, RBMKPanelPlanner.IndicatorUnit[] indicators) {
        if (indicators == null) {
            return;
        }
        int count = Math.min(INDICATOR_COUNT, indicators.length);
        for (int i = 0; i < count; i++) {
            RBMKPanelPlanner.IndicatorUnit unit = indicators[i];
            if (unit == null || !unit.active()) {
                continue;
            }
            renderIndicator(poseStack, buffer, packedLight, packedOverlay, unit, i);
        }
    }

    public static void renderIndicator(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, RBMKPanelPlanner.IndicatorUnit unit, int index) {
        poseStack.pushPose();
        translateIndicatorSlot(poseStack, index);
        renderPreparedTexturedSelection(ObjRbmkModels.INDICATOR, ObjRbmkModels.INDICATOR_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, 0xFFFFFF, INDICATOR_BASE);
        int color = scaledColor(unit.color(), unit.light() ? 1.0F : INDICATOR_DIM_MULTIPLIER);
        int light = unit.light() ? LightTexture.FULL_BRIGHT : packedLight;
        renderPreparedTexturedSelection(ObjRbmkModels.INDICATOR, ObjRbmkModels.INDICATOR_TEXTURE,
                poseStack, buffer, light, packedOverlay, color, INDICATOR_LIGHT);
        renderCenteredLegacyText(poseStack, buffer, packedLight, unit.label(),
                INDICATOR_LABEL_X, INDICATOR_LABEL_Y, 0.0D, INDICATOR_LABEL_MAX_WIDTH, 0x000000, false);
        poseStack.popPose();
    }

    public static void renderLevers(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            RBMKPanelPlanner.LeverUnit[] levers, float partialTick) {
        if (levers == null) {
            return;
        }
        int count = Math.min(LEVER_COUNT, levers.length);
        for (int i = 0; i < count; i++) {
            RBMKPanelPlanner.LeverUnit unit = levers[i];
            if (unit == null || !unit.active()) {
                continue;
            }
            renderLever(poseStack, buffer, packedLight, packedOverlay, unit, i, partialTick);
        }
    }

    public static void renderLever(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            RBMKPanelPlanner.LeverUnit unit, int index, float partialTick) {
        poseStack.pushPose();
        translateLeverSlot(poseStack, index);
        renderPreparedTexturedSelection(ObjRbmkModels.LEVER, ObjRbmkModels.LEVER_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, 0xFFFFFF, LEVER_BASE);
        poseStack.pushPose();
        poseStack.translate(LEVER_PIVOT_X, LEVER_PIVOT_Y, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(leverAngle(unit, partialTick)));
        poseStack.translate(-LEVER_PIVOT_X, -LEVER_PIVOT_Y, 0.0D);
        renderPreparedTexturedSelection(ObjRbmkModels.LEVER, ObjRbmkModels.LEVER_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, 0xFFFFFF, LEVER_HANDLE);
        poseStack.popPose();
        renderCenteredLegacyText(poseStack, buffer, packedLight, unit.label(),
                LEVER_LABEL_X, LEVER_LABEL_Y, 0.0D, LEVER_LABEL_MAX_WIDTH, 0x00FF00, true);
        poseStack.popPose();
    }

    public static float leverAngle(RBMKPanelPlanner.LeverUnit unit, float partialTick) {
        if (unit == null) {
            return 0.0F;
        }
        float progress = unit.prevFlipProgress() + (unit.flipProgress() - unit.prevFlipProgress()) * partialTick;
        return -180.0F * Mth.clamp(progress, 0.0F, 1.0F);
    }

    public static void renderNumitrons(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, RBMKPanelPlanner.NumitronUnit[] units) {
        if (units == null) {
            return;
        }
        int count = Math.min(NUMITRON_COUNT, units.length);
        for (int i = 0; i < count; i++) {
            RBMKPanelPlanner.NumitronUnit unit = units[i];
            if (unit == null || !unit.active()) {
                continue;
            }
            renderNumitron(poseStack, buffer, packedLight, packedOverlay, unit, i);
        }
    }

    public static void renderNumitron(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, RBMKPanelPlanner.NumitronUnit unit, int index) {
        poseStack.pushPose();
        translateNumitronSlot(poseStack, index);
        ObjRbmkModels.NUMITRON.renderAll(ObjRbmkModels.NUMITRON_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderNumitronDigits(poseStack, buffer, packedOverlay, unit);
        renderCenteredLegacyText(poseStack, buffer, packedLight, unit.label(),
                NUMITRON_LABEL_X, NUMITRON_LABEL_Y, 0.0D, NUMITRON_LABEL_MAX_WIDTH, 0x00FF00, true);
        poseStack.popPose();
    }

    public static void renderNumitronDigits(PoseStack poseStack, MultiBufferSource buffer, int packedOverlay,
            RBMKPanelPlanner.NumitronUnit unit) {
        String value = numitronValue(unit);
        long activeDigits = unit == null ? 0L : unit.activeDigits();
        for (int i = 0; i < NUMITRON_DIGITS; i++) {
            if ((activeDigits & (NUMITRON_LEFT_DIGIT_MASK >> i)) == 0L) {
                continue;
            }
            char character = value.charAt(i);
            DigitUv uv = digitUv(character);
            if (uv.blank()) {
                continue;
            }
            double zOffset = (i - 3) * NUMITRON_DIGIT_Z_STEP;
            LegacyTexturedQuadRenderer.quad(ObjRbmkModels.NUMITRON_LIGHTS_TEXTURE, poseStack, buffer,
                    LightTexture.FULL_BRIGHT, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                    0.0F, 1.0F, 0.0F,
                    LegacyTexturedQuadRenderer.vertex(NUMITRON_DIGIT_X, -NUMITRON_DIGIT_H + NUMITRON_DIGIT_Y,
                            NUMITRON_DIGIT_W - zOffset, uv.u(), uv.v() + 0.5D, 0xFFFFFF, 255),
                    LegacyTexturedQuadRenderer.vertex(NUMITRON_DIGIT_X, NUMITRON_DIGIT_H + NUMITRON_DIGIT_Y,
                            NUMITRON_DIGIT_W - zOffset, uv.u(), uv.v(), 0xFFFFFF, 255),
                    LegacyTexturedQuadRenderer.vertex(NUMITRON_DIGIT_X, NUMITRON_DIGIT_H + NUMITRON_DIGIT_Y,
                            -NUMITRON_DIGIT_W - zOffset, uv.u() + 0.1D, uv.v(), 0xFFFFFF, 255),
                    LegacyTexturedQuadRenderer.vertex(NUMITRON_DIGIT_X, -NUMITRON_DIGIT_H + NUMITRON_DIGIT_Y,
                            -NUMITRON_DIGIT_W - zOffset, uv.u() + 0.1D, uv.v() + 0.5D, 0xFFFFFF, 255));
        }
    }

    public static String numitronValue(RBMKPanelPlanner.NumitronUnit unit) {
        if (unit == null) {
            return "       ";
        }
        String value;
        if (unit.shortenNumber()) {
            value = HbmMathUtil.getShortNumber(unit.value());
        } else if (unit.value() > 9_999_999L) {
            value = "9999999";
        } else if (unit.value() < -999_999L) {
            value = "-999999";
        } else {
            value = Long.toString(unit.value());
        }
        if (value.isEmpty()) {
            value = " ";
        }
        if (value.length() > NUMITRON_DIGITS) {
            value = value.substring(0, NUMITRON_DIGITS);
        }
        if (value.length() < NUMITRON_DIGITS && value.charAt(0) == '-' && unit.leadingZeroes()) {
            value = value.substring(1);
            while (value.length() < NUMITRON_DIGITS - 1) {
                value = "0" + value;
            }
            return "-" + value;
        }
        String fill = unit.leadingZeroes() ? "0" : " ";
        while (value.length() < NUMITRON_DIGITS) {
            value = fill + value;
        }
        return value;
    }

    public static DigitUv digitUv(char character) {
        return switch (character) {
            case ' ' -> new DigitUv(0.0D, 0.0D, true);
            case '.' -> new DigitUv(0.9D, 0.5D, false);
            case '-' -> new DigitUv(0.8D, 0.5D, false);
            case 'k' -> new DigitUv(0.0D, 0.5D, false);
            case 'M' -> new DigitUv(0.1D, 0.5D, false);
            case 'G' -> new DigitUv(0.2D, 0.5D, false);
            case 'T' -> new DigitUv(0.3D, 0.5D, false);
            case 'P' -> new DigitUv(0.4D, 0.5D, false);
            case 'E' -> new DigitUv(0.5D, 0.5D, false);
            default -> {
                int digit = character - '0';
                if (digit >= 0 && digit <= 9) {
                    yield new DigitUv(0.1D * digit, 0.0D, false);
                }
                yield new DigitUv(0.8D, 0.5D, false);
            }
        };
    }

    public static void renderGraphs(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            RBMKPanelPlanner.GraphUnit[] graphs) {
        if (graphs == null) {
            return;
        }
        int count = Math.min(GRAPH_COUNT, graphs.length);
        for (int i = 0; i < count; i++) {
            RBMKPanelPlanner.GraphUnit unit = graphs[i];
            if (unit == null || !unit.active()) {
                continue;
            }
            renderGraph(poseStack, buffer, packedLight, packedOverlay, unit, i);
        }
    }

    public static void renderGraph(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            RBMKPanelPlanner.GraphUnit unit, int index) {
        poseStack.pushPose();
        translateGraphSlot(poseStack, index);
        ObjRbmkModels.NUMITRON.renderAll(ObjRbmkModels.NUMITRON_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        renderGraphLines(poseStack, buffer, unit);
        renderGraphLimitLabels(poseStack, buffer, unit);
        renderCenteredLegacyText(poseStack, buffer, packedLight, unit.label(),
                GRAPH_LABEL_X, GRAPH_LABEL_Y, 0.0D, GRAPH_LABEL_MAX_WIDTH, 0x00FF00, true);
        poseStack.popPose();
    }

    public static void renderGraphLines(PoseStack poseStack, MultiBufferSource buffer,
            RBMKPanelPlanner.GraphUnit unit) {
        long[] values = unit == null ? new long[0] : unit.values();
        if (values.length < 2) {
            return;
        }
        long lowest = graphLowest(unit);
        long highest = graphHighest(unit);
        long range = Math.max(highest - lowest, 1L);
        for (int i = 0; i < values.length - 1; i++) {
            double x0 = GRAPH_LINE_X;
            double y0 = graphY(clampLong(values[i], lowest, highest), lowest, range);
            double z0 = graphZ(i, values.length);
            double x1 = GRAPH_LINE_X;
            double y1 = graphY(clampLong(values[i + 1], lowest, highest), lowest, range);
            double z1 = graphZ(i + 1, values.length);
            LegacyLineRenderer.line(poseStack, buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL,
                    2.0F, x0, y0, z0, x1, y1, z1, 0x00FF00, 255);
        }
    }

    public static long graphLowest(RBMKPanelPlanner.GraphUnit unit) {
        if (unit == null) {
            return 0L;
        }
        if (unit.minBound()) {
            return unit.min();
        }
        long lowest = Long.MAX_VALUE;
        for (long value : unit.values()) {
            lowest = Math.min(lowest, value);
        }
        return lowest == Long.MAX_VALUE ? 0L : lowest;
    }

    public static long graphHighest(RBMKPanelPlanner.GraphUnit unit) {
        if (unit == null) {
            return 0L;
        }
        if (unit.maxBound()) {
            return unit.max();
        }
        long highest = Long.MIN_VALUE;
        for (long value : unit.values()) {
            highest = Math.max(highest, value);
        }
        return highest == Long.MIN_VALUE ? 0L : highest;
    }

    public static double graphY(long value, long lowest, long range) {
        return GRAPH_BASE_Y + (value - lowest) * GRAPH_HEIGHT / Math.max(range, 1L);
    }

    public static double graphZ(int index, int count) {
        return GRAPH_Z_START - index * GRAPH_Z_SPAN / Math.max(count - 1, 1);
    }

    public static void renderKeys(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            RBMKPanelPlanner.KeyUnit[] keys) {
        if (keys == null) {
            return;
        }
        int count = Math.min(KEY_COUNT, keys.length);
        for (int i = 0; i < count; i++) {
            RBMKPanelPlanner.KeyUnit unit = keys[i];
            if (unit == null || !unit.active()) {
                continue;
            }
            renderKey(poseStack, buffer, packedLight, packedOverlay, unit, i);
        }
    }

    public static void renderKey(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            RBMKPanelPlanner.KeyUnit key, int index) {
        poseStack.pushPose();
        translateKeySlot(poseStack, index);
        renderPreparedTexturedSelection(ObjRbmkModels.BUTTON, ObjRbmkModels.KEYPAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay, 0xFFFFFF, KEY_SOCKET);

        poseStack.pushPose();
        if (key.isPressed()) {
            poseStack.translate(KEY_PRESSED_X_OFFSET, 0.0D, 0.0D);
        }
        int color = scaledColor(key.color(), key.isPressed() ? 1.0F : KEY_DIM_MULTIPLIER);
        int light = key.isPressed() ? LightTexture.FULL_BRIGHT : packedLight;
        renderPreparedTexturedSelection(ObjRbmkModels.BUTTON, ObjRbmkModels.KEYPAD_TEXTURE,
                poseStack, buffer, light, packedOverlay, color, KEY_BUTTON);
        poseStack.popPose();
        renderCenteredLegacyText(poseStack, buffer, packedLight, key.label(),
                KEY_LABEL_X, KEY_LABEL_Y, 0.0D, KEY_LABEL_MAX_WIDTH, 0x00FF00, true);
        poseStack.popPose();
    }

    private static void renderGaugeLimitLabels(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            RBMKPanelPlanner.GaugeUnit unit) {
        if (unit == null) {
            return;
        }
        String lower = gaugeLimitLabel(unit.min());
        String upper = gaugeLimitLabel(unit.max());
        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            poseStack.translate(0.0D, GAUGE_PIVOT_Y, GAUGE_PIVOT_Z);
            poseStack.mulPose(Axis.XP.rotationDegrees((float) -(GAUGE_MIN_MARK_ANGLE
                    + i * (GAUGE_MAX_MARK_ANGLE - GAUGE_MIN_MARK_ANGLE))));
            poseStack.translate(0.0D, -GAUGE_PIVOT_Y, -GAUGE_PIVOT_Z);
            poseStack.translate(GAUGE_LIMIT_LABEL_X, GAUGE_LIMIT_LABEL_Y, GAUGE_LIMIT_LABEL_Z);
            drawLegacyText(poseStack, buffer, i == 0 ? lower : upper, 0.0F, -font().lineHeight / 2.0F,
                    GAUGE_LIMIT_TEXT_SCALE, 0x000000, packedLight);
            poseStack.popPose();
        }
    }

    private static String gaugeLimitLabel(long value) {
        return value <= 10_000L ? Long.toString(value) : HbmMathUtil.getShortNumber(value);
    }

    private static void renderPreparedTexturedSelection(LegacyWavefrontModel model, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, int color,
            LegacyWavefrontModel.SelectionHandle handle) {
        model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, LegacyWavefrontModel.UvTransform.DEFAULT, handle);
    }

    private static void renderGraphLimitLabels(PoseStack poseStack, MultiBufferSource buffer,
            RBMKPanelPlanner.GraphUnit unit) {
        long lowest = graphLowest(unit);
        long highest = graphHighest(unit);
        Font font = font();
        String lower = Long.toString(lowest);
        String upper = Long.toString(highest);
        poseStack.pushPose();
        poseStack.translate(GRAPH_LIMIT_LABEL_X, GRAPH_LIMIT_LABEL_Y, GRAPH_LIMIT_LABEL_Z);
        drawLegacyText(poseStack, buffer, lower, -font.width(lower), -font.lineHeight / 2.0F,
                GRAPH_LIMIT_TEXT_SCALE, 0x00FF00, LightTexture.FULL_BRIGHT);
        poseStack.translate(0.0D, GRAPH_LIMIT_LABEL_STEP_Y, 0.0D);
        drawLegacyText(poseStack, buffer, upper, -font.width(upper), -font.lineHeight / 2.0F,
                GRAPH_LIMIT_TEXT_SCALE, 0x00FF00, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void renderCenteredLegacyText(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            String text, double x, double y, double z, double maxWidth, int color, boolean fullBright) {
        if (text == null || text.isEmpty()) {
            return;
        }
        Font font = font();
        int width = font.width(text);
        float scale = (float) Math.min(0.0125D, maxWidth / Math.max(width, 1));
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        drawLegacyText(poseStack, buffer, text, -width / 2.0F, -font.lineHeight / 2.0F,
                scale, color, fullBright ? LightTexture.FULL_BRIGHT : packedLight);
        poseStack.popPose();
    }

    private static void drawLegacyText(PoseStack poseStack, MultiBufferSource buffer, String text, float x, float y,
            double scale, int color, int packedLight) {
        if (text == null || text.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale((float) scale, (float) -scale, (float) scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        font().drawInBatch(text, x, y, color, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    public static void translateGaugeSlot(PoseStack poseStack, int index) {
        poseStack.translate(PANEL_X, (index / 2) * GAUGE_ROW_STEP + GAUGE_Y_START,
                (index % 2) * GAUGE_COLUMN_STEP + GAUGE_Z_START);
    }

    public static void translateIndicatorSlot(PoseStack poseStack, int index) {
        poseStack.translate(PANEL_X, (index / 2) * INDICATOR_ROW_STEP + INDICATOR_Y_START,
                (index % 2) * INDICATOR_COLUMN_STEP + INDICATOR_Z_START);
    }

    public static void translateKeySlot(PoseStack poseStack, int index) {
        poseStack.translate(PANEL_X, (index / 2) * KEY_ROW_STEP + KEY_Y_START,
                (index % 2) * KEY_COLUMN_STEP + KEY_Z_START);
    }

    public static void translateLeverSlot(PoseStack poseStack, int index) {
        poseStack.translate(PANEL_X, 0.0D, index * LEVER_COLUMN_STEP + LEVER_Z_START);
    }

    public static void translateNumitronSlot(PoseStack poseStack, int index) {
        poseStack.translate(PANEL_X, index * NUMITRON_ROW_STEP + NUMITRON_Y_START, 0.0D);
    }

    public static void translateGraphSlot(PoseStack poseStack, int index) {
        poseStack.translate(PANEL_X, index * GRAPH_ROW_STEP + GRAPH_Y_START, 0.0D);
    }

    public static int scaledColor(int color, float multiplier) {
        return LegacyRenderColor.scale(color, multiplier);
    }

    private static long clampLong(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    public record DigitUv(double u, double v, boolean blank) {
    }

    private LegacyRbmkPanelRenderer() {
    }
}
