package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyRenderColor;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
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
    private static final String[] EMPTY_TERMINAL_HISTORY = new String[TERMINAL_HISTORY_COUNT];
    private static final String[] ASCII_CHARACTER_STRINGS = asciiCharacterStrings();

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

    public static void renderTerminalModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(TERMINAL_MODEL_X, 0.0D, 0.0D);
        ObjRbmkModels.renderTerminal(poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    public static void renderTerminalText(Font font, PoseStack poseStack, MultiBufferSource buffer,
            RBMKPanelPlanner.TerminalState state, String workingLine, boolean cursor) {
        if (font == null) {
            return;
        }
        String[] history = state == null ? EMPTY_TERMINAL_HISTORY : state.history();
        int color = state != null && !state.repeatCommand().isEmpty() ? TERMINAL_COLOR_REPEAT : TERMINAL_COLOR_NORMAL;
        poseStack.pushPose();
        poseStack.translate(TERMINAL_TEXT_X, TERMINAL_TEXT_Y, TERMINAL_TEXT_Z);
        for (int i = 0; i < TERMINAL_LINE_COUNT; i++) {
            String label = i == 0 ? workingLine : historyValue(history, i - 1);
            String clipped = clippedTerminalLine(font, label, i == 0, cursor);
            renderTerminalLine(font, poseStack, buffer, clipped, color);
        }
        poseStack.popPose();
    }

    public static void renderTerminalText(Font font, PoseStack poseStack, MultiBufferSource buffer,
            TerminalLine[] lines) {
        if (font == null || lines == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(TERMINAL_TEXT_X, TERMINAL_TEXT_Y, TERMINAL_TEXT_Z);
        for (TerminalLine line : lines) {
            if (line == null || line.text().isEmpty()) {
                poseStack.translate(0.0D, TERMINAL_TEXT_LINE_STEP, 0.0D);
                continue;
            }
            renderTerminalLine(font, poseStack, buffer, line.text(), line.color());
        }
        poseStack.popPose();
    }

    public static TerminalLine[] terminalLines(Font font, RBMKPanelPlanner.TerminalState state,
            String workingLine, boolean cursor) {
        return terminalLines(
                text -> font == null ? text.length() * 6 : font.width(text),
                character -> font == null ? 6 : terminalCharWidth(font, character),
                state,
                workingLine,
                cursor);
    }

    public static TerminalLine[] terminalLines(StringWidth stringWidth, CharWidth charWidth,
            RBMKPanelPlanner.TerminalState state, String workingLine, boolean cursor) {
        String[] history = state == null ? EMPTY_TERMINAL_HISTORY : state.history();
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

    public static String clippedTerminalLine(Font font, String label, boolean workingLine, boolean cursor) {
        String safeLabel = label == null ? "" : label;
        StringBuilder builder = new StringBuilder(40);
        if (workingLine || !safeLabel.isEmpty()) {
            builder.append(TERMINAL_PREFIX);
        }
        int width = font == null ? TERMINAL_PREFIX.length() * 6 : font.width(TERMINAL_PREFIX);
        for (int i = 0; i < safeLabel.length(); i++) {
            char character = safeLabel.charAt(i);
            width += font == null ? 6 : terminalCharWidth(font, character);
            if (width <= TERMINAL_MAX_WIDTH) {
                builder.append(character);
            } else {
                break;
            }
        }
        int cursorWidth = font == null ? TERMINAL_CURSOR.length() * 6 : font.width(TERMINAL_CURSOR);
        if (workingLine && cursor && width + cursorWidth <= TERMINAL_MAX_WIDTH) {
            builder.append(TERMINAL_CURSOR);
        }
        return builder.toString();
    }

    public static void renderCraneConsole(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, CraneConsoleState state, float partialTick, long currentMillis) {
        renderCraneConsole(poseStack, buffer, packedLight, packedOverlay, state, partialTick, currentMillis, true);
    }

    public static void renderCraneConsole(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, CraneConsoleState state, float partialTick, long currentMillis,
            boolean renderStaticBody) {
        CraneConsoleState safe = state == null ? CraneConsoleState.EMPTY : state;
        double joystickFront = interpolate(safe.lastTiltFront(), safe.tiltFront(), partialTick);
        double joystickLeft = interpolate(safe.lastTiltLeft(), safe.tiltLeft(), partialTick);
        double wobble = Math.sin((currentMillis * CRANE_METER_WOBBLE_SPEED) % 360.0D) * CRANE_METER_WOBBLE_SCALE;
        poseStack.pushPose();
        poseStack.translate(CRANE_CONSOLE_X, 0.0D, 0.0D);
        if (renderStaticBody) {
            ObjRbmkModels.renderCraneConsolePart("Console_Coonsole", poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        }
        renderCraneJoystick(poseStack, buffer, packedLight, packedOverlay, joystickFront, joystickLeft);
        renderCraneMeter(poseStack, buffer, packedLight, packedOverlay, "Meter1", CRANE_METER1_PIVOT_Z,
                wobble + CRANE_METER_BASE_ANGLE - CRANE_METER_VALUE_SPAN * safe.loadedHeat());
        renderCraneMeter(poseStack, buffer, packedLight, packedOverlay, "Meter2", CRANE_METER2_PIVOT_Z,
                wobble + CRANE_METER_BASE_ANGLE - CRANE_METER_VALUE_SPAN * safe.loadedEnrichment());
        renderCraneLamp(poseStack, buffer, "Lamp1", loadingLampColor(safe.loading(), safe.loaded()));
        renderCraneLamp(poseStack, buffer, "Lamp2", targetLampColor(safe.validTarget()));
        poseStack.popPose();
    }

    public static void renderCraneConsolePlan(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTileRenderPlans.CraneConsolePlan plan) {
        renderCraneConsolePlan(poseStack, buffer, packedLight, packedOverlay, plan, true);
    }

    public static void renderCraneConsolePlan(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTileRenderPlans.CraneConsolePlan plan, boolean renderStaticBody) {
        if (plan == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(plan.translateX(), 0.0D, 0.0D);
        if (renderStaticBody) {
            ObjRbmkModels.renderCraneConsolePart("Console_Coonsole", poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        }
        renderCraneJoystick(poseStack, buffer, packedLight, packedOverlay, plan.joystick());
        renderCraneMeter(poseStack, buffer, packedLight, packedOverlay, plan.meterHeat());
        renderCraneMeter(poseStack, buffer, packedLight, packedOverlay, plan.meterEnrichment());
        renderCraneLamp(poseStack, buffer, plan.loadingLamp());
        renderCraneLamp(poseStack, buffer, plan.targetLamp());
        poseStack.popPose();
    }

    public static void renderCraneJoystick(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, double tiltFrontDegrees, double tiltLeftDegrees) {
        poseStack.pushPose();
        poseStack.translate(CRANE_JOYSTICK_PIVOT_X, CRANE_JOYSTICK_PIVOT_Y, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) tiltFrontDegrees));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) tiltLeftDegrees));
        poseStack.translate(-CRANE_JOYSTICK_PIVOT_X, CRANE_JOYSTICK_RETURN_Y, 0.0D);
        ObjRbmkModels.renderCraneConsolePart("Joystick", poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    public static void renderCraneJoystick(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTileRenderPlans.CraneJoystickPlan plan) {
        if (plan == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(plan.pivotX(), plan.pivotY(), 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) plan.tiltFrontDegrees()));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) plan.tiltLeftDegrees()));
        poseStack.translate(-plan.pivotX(), plan.restoreY(), 0.0D);
        ObjRbmkModels.renderCraneConsolePart("Joystick", poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    public static void renderCraneMeter(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, String partName, double pivotZ, double angleDegrees) {
        poseStack.pushPose();
        poseStack.translate(0.0D, CRANE_METER_PIVOT_Y, pivotZ);
        poseStack.mulPose(Axis.XP.rotationDegrees((float) angleDegrees));
        poseStack.translate(0.0D, -CRANE_METER_PIVOT_Y, -pivotZ);
        ObjRbmkModels.renderCraneConsolePart(partName, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    public static void renderCraneMeter(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTileRenderPlans.CraneMeterPlan plan) {
        if (plan == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, plan.pivotY(), plan.pivotZ());
        poseStack.mulPose(Axis.XP.rotationDegrees((float) plan.angleDegrees()));
        poseStack.translate(0.0D, -plan.pivotY(), -plan.pivotZ());
        ObjRbmkModels.renderCraneConsolePart(plan.partName(), poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    public static void renderCraneLamp(PoseStack poseStack, MultiBufferSource buffer, String partName, int color) {
        ObjRbmkModels.renderCraneConsolePartUntextured(partName, poseStack, buffer,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255);
    }

    public static void renderCraneLamp(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTileRenderPlans.ModelPartTintPlan plan) {
        if (plan == null || !plan.active()) {
            return;
        }
        LegacyTileRenderPlans.RgbaPlan color = plan.color();
        ObjRbmkModels.renderCraneConsolePartUntextured(plan.partName(), poseStack, buffer,
                color.redByte(), color.greenByte(), color.blueByte(), color.alphaByte());
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

    public static void renderCrane(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            CraneState state, float partialTick) {
        if (state == null || !state.setUp()) {
            return;
        }
        double posFront = interpolate(state.lastPosFront(), state.posFront(), partialTick);
        double posLeft = interpolate(state.lastPosLeft(), state.posLeft(), partialTick);
        double progress = interpolate(state.lastProgress(), state.progress(), partialTick);

        poseStack.pushPose();
        poseStack.translate(-posFront, 0.0D, posLeft);
        poseStack.mulPose(Axis.YP.rotationDegrees(normalizedCraneRotation(state.craneRotationOffset())));
        renderCraneGirder(poseStack, buffer, packedLight, packedOverlay, state.spans(), posFront, posLeft,
                state.craneRotationOffset());
        ObjRbmkModels.renderCranePart("Main", poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        renderCraneTubeAndCarriage(poseStack, buffer, packedLight, packedOverlay, craneTubeHeight(state.height()));
        poseStack.translate(0.0D, craneLiftY(progress), 0.0D);
        ObjRbmkModels.renderCranePart("Lift", poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        poseStack.popPose();
    }

    public static void renderCraneGirder(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, CraneSpans spans, double posFront, double posLeft, int craneRotationOffset) {
        CraneSpans safe = spans == null ? CraneSpans.ZERO : spans;
        GirderPlan plan = girderPlan(safe, posFront, posLeft, craneRotationOffset);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-normalizedCraneRotation(craneRotationOffset)));
        poseStack.translate(plan.translateX(), 0.0D, plan.translateZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(normalizedCraneRotation(craneRotationOffset)));
        for (int i = 0; i < plan.span(); i++) {
            ObjRbmkModels.renderCranePart("Girder", poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            poseStack.translate(-1.0D, 0.0D, 0.0D);
        }
        poseStack.popPose();
    }

    public static void renderCraneTubeAndCarriage(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, int tubeHeight) {
        poseStack.pushPose();
        int height = Math.max(0, tubeHeight);
        for (int i = 0; i < height; i++) {
            ObjRbmkModels.renderCranePart("Tube", poseStack, buffer, packedLight, packedOverlay,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            poseStack.translate(0.0D, 1.0D, 0.0D);
        }
        poseStack.translate(0.0D, -1.0D, 0.0D);
        ObjRbmkModels.renderCranePart("Carriage", poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
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

    public static void renderAutoloader(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, double lastPiston, double renderPiston, float partialTick,
            LegacyTexturedRenderMode renderMode) {
        renderAutoloaderBase(poseStack, buffer, packedLight, packedOverlay, renderMode);
        renderAutoloaderPiston(poseStack, buffer, packedLight, packedOverlay, lastPiston, renderPiston, partialTick,
                renderMode);
    }

    public static void renderAutoloaderBase(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, LegacyTexturedRenderMode renderMode) {
        ObjRbmkModels.renderAutoloaderPart("Base", poseStack, buffer, packedLight, packedOverlay, renderMode);
    }

    public static void renderAutoloaderPiston(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, double lastPiston, double renderPiston, float partialTick,
            LegacyTexturedRenderMode renderMode) {
        poseStack.pushPose();
        poseStack.translate(0.0D, autoloaderPistonY(lastPiston, renderPiston, partialTick), 0.0D);
        ObjRbmkModels.renderAutoloaderPart("Piston", poseStack, buffer, packedLight, packedOverlay, renderMode);
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

    private static void renderTerminalLine(Font font, PoseStack poseStack, MultiBufferSource buffer,
            String text, int color) {
        poseStack.translate(0.0D, TERMINAL_TEXT_LINE_STEP, 0.0D);
        if (text == null || text.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(TERMINAL_TEXT_SCALE, -TERMINAL_TEXT_SCALE, TERMINAL_TEXT_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        font.drawInBatch(text, 0.0F, -font.lineHeight / 2.0F, color, false,
                poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static int terminalCharWidth(Font font, char character) {
        return font.width(character < ASCII_CHARACTER_STRINGS.length
                ? ASCII_CHARACTER_STRINGS[character] : String.valueOf(character));
    }

    private static String[] asciiCharacterStrings() {
        String[] strings = new String[128];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = Character.toString((char) i);
        }
        return strings;
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
