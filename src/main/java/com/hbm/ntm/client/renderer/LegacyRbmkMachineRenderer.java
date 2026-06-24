package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyRenderColor;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.neutron.RBMKControlRodPlanner;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class LegacyRbmkMachineRenderer {
    public static final double TERMINAL_MODEL_X = 0.25D;
    public static final double TERMINAL_TEXT_X = 0.0635D;
    public static final double TERMINAL_TEXT_Y = 0.125D;
    public static final double TERMINAL_TEXT_Z = 0.0625D * 5.5D;
    public static final float TERMINAL_TEXT_SCALE = 1.0F / 250.0F;
    public static final double TERMINAL_TEXT_LINE_STEP = 10.0D * TERMINAL_TEXT_SCALE;
    public static final int TERMINAL_LINE_COUNT = 18;
    public static final int TERMINAL_HISTORY_COUNT = TERMINAL_LINE_COUNT - 1;
    public static final String TERMINAL_PREFIX = "> ";
    public static final String TERMINAL_CURSOR = "_";
    public static final int TERMINAL_MAX_WIDTH = 172;
    public static final int TERMINAL_COLOR_NORMAL = 0x00FF00;
    public static final int TERMINAL_COLOR_REPEAT = 0xFF8000;

    public static final double CRANE_CONSOLE_X = 0.5D;
    public static final double CRANE_JOYSTICK_PIVOT_X = 0.75D;
    public static final double CRANE_JOYSTICK_PIVOT_Y = 1.0D;
    public static final double CRANE_JOYSTICK_RETURN_Y = -1.015D;
    public static final double CRANE_METER_PIVOT_Y = 1.25D;
    public static final double CRANE_METER1_PIVOT_Z = 0.75D;
    public static final double CRANE_METER2_PIVOT_Z = 0.25D;
    public static final double CRANE_METER_BASE_ANGLE = 135.0D;
    public static final double CRANE_METER_VALUE_SPAN = 270.0D;
    public static final double CRANE_METER_WOBBLE_SPEED = 0.01D;
    public static final double CRANE_METER_WOBBLE_SCALE = 180.0D / Math.PI * 0.05D;
    public static final int CRANE_LAMP_LOADING = LegacyRenderColor.color(0.8F, 0.8F, 0.0F);
    public static final int CRANE_LAMP_LOADED = 0x00FF00;
    public static final int CRANE_LAMP_UNLOADED = LegacyRenderColor.color(0.0F, 0.1F, 0.0F);
    public static final int CRANE_LAMP_VALID_TARGET = 0x00FF00;
    public static final int CRANE_LAMP_INVALID_TARGET = 0xFF0000;
    public static final double CRANE_LIFT_RETRACTED_Y = -3.25D;

    public static final double AUTOLOADER_PISTON_SCALE = -4.0D;
    public static final double AUTOLOADER_PISTON_BASE_Y = 4.0D;
    public static final double AUTOLOADER_INVENTORY_Y = -6.0D;
    public static final double AUTOLOADER_INVENTORY_SCALE = 1.75D;

    public static void renderTerminalModel(ObjRenderContext context) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(TERMINAL_MODEL_X, 0.0D, 0.0D);
        ObjRbmkModels.TERMINAL.renderAll(ObjRbmkModels.TERMINAL_TEXTURE, context);
        poseStack.popPose();
    }

    public static void renderTerminalText(Font font, ObjRenderContext context, RBMKPanelPlanner.TerminalState state,
            String workingLine, boolean cursor) {
        renderTerminalText(font, context, terminalLines(font, state, workingLine, cursor));
    }

    public static void renderTerminalText(Font font, ObjRenderContext context, TerminalLine[] lines) {
        if (font == null || lines == null) {
            return;
        }
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(TERMINAL_TEXT_X, TERMINAL_TEXT_Y, TERMINAL_TEXT_Z);
        for (TerminalLine line : lines) {
            if (line == null || line.text().isEmpty()) {
                poseStack.translate(0.0D, TERMINAL_TEXT_LINE_STEP, 0.0D);
                continue;
            }
            poseStack.translate(0.0D, TERMINAL_TEXT_LINE_STEP, 0.0D);
            poseStack.pushPose();
            poseStack.scale(TERMINAL_TEXT_SCALE, -TERMINAL_TEXT_SCALE, TERMINAL_TEXT_SCALE);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            font.drawInBatch(line.text(), 0.0F, -font.lineHeight / 2.0F, line.color(), false,
                    poseStack.last().pose(), context.buffer(), Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static TerminalLine[] terminalLines(Font font, RBMKPanelPlanner.TerminalState state,
            String workingLine, boolean cursor) {
        return terminalLines(
                text -> font == null ? text.length() * 6 : font.width(text),
                character -> font == null ? 6 : font.width(String.valueOf(character)),
                state,
                workingLine,
                cursor);
    }

    public static TerminalLine[] terminalLines(StringWidth stringWidth, CharWidth charWidth,
            RBMKPanelPlanner.TerminalState state, String workingLine, boolean cursor) {
        String[] history = state == null ? new String[TERMINAL_HISTORY_COUNT] : state.history();
        boolean repeating = state != null && !state.repeatCommand().isEmpty();
        TerminalLine[] lines = new TerminalLine[TERMINAL_LINE_COUNT];
        int color = repeating ? TERMINAL_COLOR_REPEAT : TERMINAL_COLOR_NORMAL;
        for (int i = 0; i < lines.length; i++) {
            String label = i == 0 ? workingLine : historyValue(history, i - 1);
            String clipped = clippedTerminalLine(stringWidth, charWidth, label, i == 0, cursor);
            lines[i] = new TerminalLine(clipped, color, i);
        }
        return lines;
    }

    public static String clippedTerminalLine(StringWidth stringWidth, CharWidth charWidth,
            String label, boolean workingLine, boolean cursor) {
        String safeLabel = label == null ? "" : label;
        StringBuilder builder = new StringBuilder(40);
        if (workingLine || !safeLabel.isEmpty()) {
            builder.append(TERMINAL_PREFIX);
        }
        int width = safeStringWidth(stringWidth, TERMINAL_PREFIX);
        for (int i = 0; i < safeLabel.length(); i++) {
            char character = safeLabel.charAt(i);
            width += safeCharWidth(charWidth, character);
            if (width <= TERMINAL_MAX_WIDTH) {
                builder.append(character);
            } else {
                break;
            }
        }
        if (workingLine && cursor
                && safeStringWidth(stringWidth, builder.toString()) + safeStringWidth(stringWidth, TERMINAL_CURSOR)
                <= TERMINAL_MAX_WIDTH) {
            builder.append(TERMINAL_CURSOR);
        }
        return builder.toString();
    }

    public static void renderCraneConsole(ObjRenderContext context, CraneConsoleState state, float partialTick,
            long currentMillis) {
        CraneConsoleState safe = state == null ? CraneConsoleState.EMPTY : state;
        renderCraneConsolePlan(context, LegacyTileRenderPlans.craneConsolePlan(
                safe.lastTiltFront(), safe.tiltFront(), safe.lastTiltLeft(), safe.tiltLeft(),
                safe.loadedHeat(), safe.loadedEnrichment(), safe.loading(), safe.loaded(), safe.validTarget(),
                null, currentMillis, partialTick));
    }

    public static void renderCraneConsolePlan(ObjRenderContext context, LegacyTileRenderPlans.CraneConsolePlan plan) {
        if (plan == null) {
            return;
        }
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(plan.translateX(), 0.0D, 0.0D);
        ObjRbmkModels.renderCraneConsolePart("Console_Coonsole", context);
        renderCraneJoystick(context, plan.joystick());
        renderCraneMeter(context, plan.meterHeat());
        renderCraneMeter(context, plan.meterEnrichment());
        renderCraneLamp(context, plan.loadingLamp());
        renderCraneLamp(context, plan.targetLamp());
        poseStack.popPose();
    }

    public static void renderCraneJoystick(ObjRenderContext context, double lastTiltFront, double tiltFront,
            double lastTiltLeft, double tiltLeft, float partialTick) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(CRANE_JOYSTICK_PIVOT_X, CRANE_JOYSTICK_PIVOT_Y, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) interpolate(lastTiltFront, tiltFront, partialTick)));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) interpolate(lastTiltLeft, tiltLeft, partialTick)));
        poseStack.translate(-CRANE_JOYSTICK_PIVOT_X, CRANE_JOYSTICK_RETURN_Y, 0.0D);
        ObjRbmkModels.renderCraneConsolePart("Joystick", context);
        poseStack.popPose();
    }

    public static void renderCraneJoystick(ObjRenderContext context, LegacyTileRenderPlans.CraneJoystickPlan plan) {
        if (plan == null) {
            return;
        }
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(plan.pivotX(), plan.pivotY(), 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) plan.tiltFrontDegrees()));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) plan.tiltLeftDegrees()));
        poseStack.translate(-plan.pivotX(), plan.restoreY(), 0.0D);
        ObjRbmkModels.renderCraneConsolePart("Joystick", context);
        poseStack.popPose();
    }

    public static void renderCraneMeter(ObjRenderContext context, String partName, double pivotZ, double value,
            long currentMillis) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.0D, CRANE_METER_PIVOT_Y, pivotZ);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) craneMeterAngle(value, currentMillis)));
        poseStack.translate(0.0D, -CRANE_METER_PIVOT_Y, -pivotZ);
        ObjRbmkModels.renderCraneConsolePart(partName, context);
        poseStack.popPose();
    }

    public static void renderCraneMeter(ObjRenderContext context, LegacyTileRenderPlans.CraneMeterPlan plan) {
        if (plan == null) {
            return;
        }
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.0D, plan.pivotY(), plan.pivotZ());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) plan.angleDegrees()));
        poseStack.translate(0.0D, -plan.pivotY(), -plan.pivotZ());
        ObjRbmkModels.renderCraneConsolePart(plan.partName(), context);
        poseStack.popPose();
    }

    public static void renderCraneLamp(ObjRenderContext context, String partName, int color) {
        ObjRbmkModels.renderCraneConsolePartUntextured(partName, context.fullBright().withColor(color));
    }

    public static void renderCraneLamp(ObjRenderContext context, LegacyTileRenderPlans.ModelPartTintPlan plan) {
        if (plan == null || !plan.active()) {
            return;
        }
        LegacyTileRenderPlans.RgbaPlan color = plan.color();
        ObjRbmkModels.renderCraneConsolePartUntextured(plan.partName(),
                context.fullBright().withRgba(color.redByte(), color.greenByte(), color.blueByte(), color.alphaByte()));
    }

    public static double craneMeterAngle(double value, long currentMillis) {
        double clampedValue = Mth.clamp(value, 0.0D, 1.0D);
        return Math.sin(currentMillis * CRANE_METER_WOBBLE_SPEED % 360.0D) * CRANE_METER_WOBBLE_SCALE
                + CRANE_METER_BASE_ANGLE - CRANE_METER_VALUE_SPAN * clampedValue;
    }

    public static int loadingLampColor(boolean loading, boolean loaded) {
        if (loading) {
            return CRANE_LAMP_LOADING;
        }
        return loaded ? CRANE_LAMP_LOADED : CRANE_LAMP_UNLOADED;
    }

    public static int targetLampColor(boolean validTarget) {
        return validTarget ? CRANE_LAMP_VALID_TARGET : CRANE_LAMP_INVALID_TARGET;
    }

    public static void renderCrane(ObjRenderContext context, CraneState state, float partialTick) {
        if (state == null || !state.setUp()) {
            return;
        }
        double posFront = interpolate(state.lastPosFront(), state.posFront(), partialTick);
        double posLeft = interpolate(state.lastPosLeft(), state.posLeft(), partialTick);
        double progress = interpolate(state.lastProgress(), state.progress(), partialTick);

        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(-posFront, 0.0D, posLeft);
        poseStack.mulPose(Axis.YP.rotationDegrees(normalizedCraneRotation(state.craneRotationOffset())));
        renderCraneGirder(context, state.spans(), posFront, posLeft, state.craneRotationOffset());
        ObjRbmkModels.renderCranePart("Main", context);
        renderCraneTubeAndCarriage(context, craneTubeHeight(state.height()));
        poseStack.translate(0.0D, craneLiftY(progress), 0.0D);
        ObjRbmkModels.renderCranePart("Lift", context);
        poseStack.popPose();
    }

    public static void renderCraneGirder(ObjRenderContext context, CraneSpans spans, double posFront, double posLeft,
            int craneRotationOffset) {
        CraneSpans safe = spans == null ? CraneSpans.ZERO : spans;
        GirderPlan plan = girderPlan(safe, posFront, posLeft, craneRotationOffset);
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-normalizedCraneRotation(craneRotationOffset)));
        poseStack.translate(plan.translateX(), 0.0D, plan.translateZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(normalizedCraneRotation(craneRotationOffset)));
        for (int i = 0; i < plan.span(); i++) {
            ObjRbmkModels.renderCranePart("Girder", context);
            poseStack.translate(-1.0D, 0.0D, 0.0D);
        }
        poseStack.popPose();
    }

    public static void renderCraneTubeAndCarriage(ObjRenderContext context, int tubeHeight) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        int height = Math.max(0, tubeHeight);
        for (int i = 0; i < height; i++) {
            ObjRbmkModels.renderCranePart("Tube", context);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }
        poseStack.translate(0.0D, -1.0D, 0.0D);
        ObjRbmkModels.renderCranePart("Carriage", context);
        poseStack.popPose();
    }

    public static GirderPlan girderPlan(CraneSpans spans, double posFront, double posLeft, int craneRotationOffset) {
        CraneSpans safe = spans == null ? CraneSpans.ZERO : spans;
        return switch (normalizedCraneRotation(craneRotationOffset)) {
            case 90 -> new GirderPlan(safe.spanLeft() + safe.spanRight() + 1, 0.0D, -posLeft - safe.spanRight());
            case 180 -> new GirderPlan(safe.spanFront() + safe.spanBack() + 1, posFront - safe.spanFront(), 0.0D);
            case 270 -> new GirderPlan(safe.spanLeft() + safe.spanRight() + 1, 0.0D, -posLeft + safe.spanLeft());
            default -> new GirderPlan(safe.spanFront() + safe.spanBack() + 1, posFront + safe.spanBack(), 0.0D);
        };
    }

    public static int craneTubeHeight(int consoleHeight) {
        return Math.max(0, consoleHeight - 6);
    }

    public static double craneLiftY(double progress) {
        return CRANE_LIFT_RETRACTED_Y * (1.0D - Mth.clamp(progress, 0.0D, 1.0D));
    }

    public static int normalizedCraneRotation(int craneRotationOffset) {
        int normalized = Math.floorMod(craneRotationOffset, 360);
        return normalized / 90 * 90;
    }

    public static void renderAutoloader(ObjRenderContext context, double lastPiston, double renderPiston,
            float partialTick) {
        ObjRbmkModels.renderAutoloaderPart("Base", context);
        renderAutoloaderPiston(context, lastPiston, renderPiston, partialTick);
    }

    public static void renderAutoloaderPiston(ObjRenderContext context, double lastPiston, double renderPiston,
            float partialTick) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.0D, autoloaderPistonY(lastPiston, renderPiston, partialTick), 0.0D);
        ObjRbmkModels.renderAutoloaderPart("Piston", context);
        poseStack.popPose();
    }

    public static void applyAutoloaderInventoryTransform(PoseStack poseStack) {
        poseStack.translate(0.0D, AUTOLOADER_INVENTORY_Y, 0.0D);
        poseStack.scale((float) AUTOLOADER_INVENTORY_SCALE, (float) AUTOLOADER_INVENTORY_SCALE,
                (float) AUTOLOADER_INVENTORY_SCALE);
    }

    public static double autoloaderPistonY(double lastPiston, double renderPiston, float partialTick) {
        return AUTOLOADER_PISTON_BASE_Y
                + Mth.clamp(interpolate(lastPiston, renderPiston, partialTick), 0.0D, 1.0D) * AUTOLOADER_PISTON_SCALE;
    }

    public static void renderControlLid(ObjRenderContext context, boolean automatic,
            RBMKControlRodPlanner.RBMKColor manualColor, double stackOffset, double lastLevel, double level,
            float partialTick) {
        renderControlLid(context, controlTexture(automatic, manualColor), stackOffset,
                interpolate(lastLevel, level, partialTick));
    }

    public static void renderControlLid(ObjRenderContext context, ResourceLocation texture, double stackOffset,
            double level) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.0D, stackOffset + level, 0.0D);
        ObjRbmkModels.renderControlLid(texture, poseStack, context.buffer(), context.packedLight(), context.packedOverlay());
        poseStack.popPose();
    }

    public static ResourceLocation controlTexture(boolean automatic, RBMKControlRodPlanner.RBMKColor manualColor) {
        if (automatic) {
            return ObjRbmkModels.CONTROL_AUTO_TEXTURE;
        }
        return manualColor == null
                ? ObjRbmkModels.CONTROL_STANDARD_TEXTURE
                : ObjRbmkModels.manualControlTexture(manualColor.ordinal());
    }

    public static double interpolate(double previous, double current, float partialTick) {
        return previous + (current - previous) * partialTick;
    }

    private static String historyValue(String[] history, int index) {
        return history != null && index >= 0 && index < history.length && history[index] != null ? history[index] : "";
    }

    private static int safeStringWidth(StringWidth stringWidth, String text) {
        return stringWidth == null ? text.length() * 6 : stringWidth.width(text);
    }

    private static int safeCharWidth(CharWidth charWidth, char character) {
        return charWidth == null ? 6 : charWidth.width(character);
    }

    public interface StringWidth {
        int width(String text);
    }

    public interface CharWidth {
        int width(char character);
    }

    public record TerminalLine(String text, int color, int index) {
    }

    public record CraneConsoleState(
            double lastTiltFront,
            double tiltFront,
            double lastTiltLeft,
            double tiltLeft,
            double loadedHeat,
            double loadedEnrichment,
            boolean loading,
            boolean loaded,
            boolean validTarget) {
        public static final CraneConsoleState EMPTY =
                new CraneConsoleState(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, false, false, false);
    }

    public record CraneState(
            boolean setUp,
            int height,
            CraneSpans spans,
            double lastPosFront,
            double posFront,
            double lastPosLeft,
            double posLeft,
            double lastProgress,
            double progress,
            int craneRotationOffset) {
    }

    public record CraneSpans(int spanFront, int spanBack, int spanLeft, int spanRight) {
        public static final CraneSpans ZERO = new CraneSpans(0, 0, 0, 0);
    }

    public record GirderPlan(int span, double translateX, double translateZ) {
    }

    private LegacyRbmkMachineRenderer() {
    }
}
