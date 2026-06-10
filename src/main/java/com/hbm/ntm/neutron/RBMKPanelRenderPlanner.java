package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKPanelRenderPlanner {
    public static final double BASE_X_OFFSET = 0.5D;
    public static final double BASE_Z_OFFSET = 0.5D;
    public static final int FULL_BRIGHT = 240;

    public static final double PANEL_X = 0.25D;
    public static final double TWO_BY_TWO_ROW_STEP = -0.5D;
    public static final double TWO_BY_TWO_COLUMN_STEP = -0.5D;
    public static final double TWO_BY_TWO_Y_START = 0.25D;
    public static final double TWO_BY_TWO_Z_START = 0.25D;

    public static final double INDICATOR_ROW_STEP = -0.3125D;
    public static final double INDICATOR_Y_START = 0.3125D;
    public static final float INDICATOR_DIM_MULTIPLIER = 0.35F;
    public static final float KEY_DIM_MULTIPLIER = 0.65F;
    public static final double KEY_PRESSED_X_OFFSET = -0.03125D;

    public static final double GAUGE_PIVOT_Y = 0.4375D;
    public static final double GAUGE_PIVOT_Z = -0.125D;
    public static final double GAUGE_NEEDLE_SPAN_DEGREES = 50.0D;
    public static final double GAUGE_NEEDLE_BASE_DEGREES = -85.0D;
    public static final double GAUGE_NEEDLE_MAX_DEGREES = 80.0D;
    public static final double GAUGE_MIN_MARK_DEGREES = 10.0D;
    public static final double GAUGE_MAX_MARK_DEGREES = 60.0D;

    public static final double LEVER_PIVOT_X = 0.125D;
    public static final double LEVER_PIVOT_Y = 0.5625D;
    public static final double LEVER_ROTATION_DEGREES = -180.0D;

    public static final int NUMITRON_DIGITS = 7;
    public static final long NUMITRON_LEFT_DIGIT_MASK = 0x40L;
    public static final double NUMITRON_DIGIT_X = 0.03135D;
    public static final double NUMITRON_DIGIT_Y = 0.5625D;
    public static final double NUMITRON_DIGIT_SCALE = 200.0D;
    public static final double NUMITRON_DIGIT_WIDTH = 8.0D / NUMITRON_DIGIT_SCALE;
    public static final double NUMITRON_DIGIT_HEIGHT = 13.0D / NUMITRON_DIGIT_SCALE;
    public static final double NUMITRON_DIGIT_Z_STEP = 0.1D;

    public static final double GRAPH_LINE_X = 0.03225D;
    public static final double GRAPH_BASE_Y = 0.5D - 0.03125D;
    public static final double GRAPH_HEIGHT = 0.1875D;
    public static final double GRAPH_Z_START = 0.375D;
    public static final double GRAPH_Z_SPAN = 0.75D;

    public static final double TERMINAL_LOCAL_X = 0.25D;
    public static final double TERMINAL_TEXT_X = 0.0635D;
    public static final double TERMINAL_TEXT_Y = 0.125D;
    public static final double TERMINAL_TEXT_Z = 0.0625D * 5.5D;
    public static final float TERMINAL_TEXT_SCALE = 1.0F / 250.0F;
    public static final int TERMINAL_MAX_TEXT_WIDTH = 172;
    public static final int TERMINAL_RENDER_LINES = 18;
    public static final int TERMINAL_REPEAT_COLOR = 0xff8000;
    public static final int TERMINAL_NORMAL_COLOR = 0x00ff00;

    public static final double DISPLAY_Y_TRANSLATE = 0.5D;
    public static final double DISPLAY_SCALE_YZ = 8.0D / 7.0D;
    public static final double DISPLAY_COLUMN_X = 0.28125D;
    public static final double DISPLAY_COLUMN_WIDTH = 0.0625D * 0.75D;
    public static final double DISPLAY_DOT_WIDTH = 0.03125D;
    public static final double DISPLAY_DOT_EDGE = 0.022097D;
    public static final int DISPLAY_INDICATOR_COLOR = 0xffff00;

    private RBMKPanelRenderPlanner() {
    }

    public static BaseTransform baseTransform(int legacyMetadata) {
        double yaw = switch (legacyMetadata) {
            case 2 -> 90.0D;
            case 4 -> 180.0D;
            case 3 -> 270.0D;
            case 5 -> 0.0D;
            default -> 0.0D;
        };
        return new BaseTransform(BASE_X_OFFSET, 0.0D, BASE_Z_OFFSET, yaw, true, true);
    }

    public static PanelRenderContract renderContract(RBMKPanelPlanner.PanelType type) {
        RBMKPanelPlanner.PanelType safeType = type == null ? RBMKPanelPlanner.PanelType.GAUGE : type;
        return switch (safeType) {
            case GAUGE -> new PanelRenderContract(safeType, "rbmk_gauge", "rbmk_gauge_tex",
                    List.of("Gauge", "Needle"), RBMKPanelPlanner.GAUGE_COUNT, true);
            case GRAPH -> new PanelRenderContract(safeType, "rbmk_numitron", "rbmk_numitron_tex",
                    List.of("all", "graph_lines"), RBMKPanelPlanner.GRAPH_COUNT, true);
            case INDICATOR -> new PanelRenderContract(safeType, "rbmk_indicator", "rbmk_indicator_tex",
                    List.of("Base", "Light"), RBMKPanelPlanner.INDICATOR_COUNT, true);
            case KEYPAD -> new PanelRenderContract(safeType, "rbmk_button", "rbmk_keypad_tex",
                    List.of("Socket", "Button"), RBMKPanelPlanner.KEY_COUNT, true);
            case LEVER -> new PanelRenderContract(safeType, "rbmk_lever", "rbmk_lever_tex",
                    List.of("Base", "Lever"), RBMKPanelPlanner.LEVER_COUNT, true);
            case NUMITRON -> new PanelRenderContract(safeType, "rbmk_numitron", "rbmk_numitron_tex",
                    List.of("all", "digit_quads"), RBMKPanelPlanner.NUMITRON_COUNT, true);
            case TERMINAL -> new PanelRenderContract(safeType, "rbmk_terminal", "rbmk_terminal_tex",
                    List.of("all", "terminal_text"), 1, false);
            case DISPLAY -> new PanelRenderContract(safeType, "tessellator_quads", "",
                    List.of("columns", "fuel_dot", "control_dot"), RBMKPanelPlanner.DISPLAY_COLUMN_COUNT, false);
        };
    }

    public static UnitTransform unitTransform(RBMKPanelPlanner.PanelType type, int index) {
        RBMKPanelPlanner.PanelType safeType = type == null ? RBMKPanelPlanner.PanelType.GAUGE : type;
        int safeIndex = Math.max(0, index);
        return switch (safeType) {
            case GAUGE, KEYPAD -> twoByTwoTransform(safeIndex);
            case INDICATOR -> new UnitTransform(PANEL_X, (safeIndex / 2) * INDICATOR_ROW_STEP + INDICATOR_Y_START,
                    (safeIndex % 2) * TWO_BY_TWO_COLUMN_STEP + TWO_BY_TWO_Z_START);
            case GRAPH, NUMITRON -> new UnitTransform(PANEL_X, safeIndex * TWO_BY_TWO_ROW_STEP + TWO_BY_TWO_Y_START,
                    0.0D);
            case LEVER -> new UnitTransform(PANEL_X, 0.0D, safeIndex * TWO_BY_TWO_COLUMN_STEP + TWO_BY_TWO_Z_START);
            case TERMINAL -> new UnitTransform(TERMINAL_LOCAL_X, 0.0D, 0.0D);
            case DISPLAY -> new UnitTransform(0.0D, 0.0D, 0.0D);
        };
    }

    public static GaugeNeedlePlan gaugeNeedlePlan(RBMKPanelPlanner.GaugeUnit unit, float partialTick) {
        if (unit == null) {
            return new GaugeNeedlePlan(0.0D, GAUGE_NEEDLE_BASE_DEGREES, GAUGE_PIVOT_Y, GAUGE_PIVOT_Z, false);
        }
        double value = unit.lastRenderValue() + (unit.renderValue() - unit.lastRenderValue()) * partialTick;
        long lower = Math.min(unit.min(), unit.max());
        long upper = Math.max(unit.min(), unit.max());
        if (lower == upper) {
            upper++;
        }
        double angle = (value - lower) / (double) (upper - lower) * GAUGE_NEEDLE_SPAN_DEGREES;
        if (unit.min() > unit.max()) {
            angle = GAUGE_NEEDLE_SPAN_DEGREES - angle;
        }
        angle = clamp(angle, 0.0D, GAUGE_NEEDLE_MAX_DEGREES);
        return new GaugeNeedlePlan(value, angle + GAUGE_NEEDLE_BASE_DEGREES, GAUGE_PIVOT_Y, GAUGE_PIVOT_Z, true);
    }

    public static GraphRenderPlan graphRenderPlan(RBMKPanelPlanner.GraphUnit unit) {
        if (unit == null) {
            return new GraphRenderPlan(0L, 1L, List.of());
        }
        long[] values = unit.values();
        long lowest = unit.minBound() ? unit.min() : min(values);
        long highest = unit.maxBound() ? unit.max() : max(values);
        long range = Math.max(1L, highest - lowest);
        List<GraphSegment> segments = new ArrayList<>();
        for (int v = 0; v < values.length - 1; v++) {
            GraphPoint first = graphPoint(v, legacyGraphClamp(values[v], lowest, highest), lowest, range, values.length);
            GraphPoint second = graphPoint(v + 1, legacyGraphClamp(values[v + 1], lowest, highest), lowest, range,
                    values.length);
            segments.add(new GraphSegment(first, second));
        }
        return new GraphRenderPlan(lowest, highest, List.copyOf(segments));
    }

    public static IndicatorLightPlan indicatorLightPlan(RBMKPanelPlanner.IndicatorUnit unit) {
        if (unit == null) {
            return new IndicatorLightPlan(0xffffff, false, INDICATOR_DIM_MULTIPLIER);
        }
        float multiplier = unit.light() ? 1.0F : INDICATOR_DIM_MULTIPLIER;
        return new IndicatorLightPlan(scaleColor(unit.color(), multiplier), unit.light(), multiplier);
    }

    public static KeyButtonPlan keyButtonPlan(RBMKPanelPlanner.KeyUnit unit) {
        if (unit == null) {
            return new KeyButtonPlan(0xffffff, 0.0D, false, KEY_DIM_MULTIPLIER);
        }
        boolean glow = unit.isPressed();
        float multiplier = glow ? 1.0F : KEY_DIM_MULTIPLIER;
        return new KeyButtonPlan(scaleColor(unit.color(), multiplier), glow ? KEY_PRESSED_X_OFFSET : 0.0D,
                glow, multiplier);
    }

    public static LeverRenderPlan leverRenderPlan(RBMKPanelPlanner.LeverUnit unit, float partialTick) {
        if (unit == null) {
            return new LeverRenderPlan(0.0F, LEVER_PIVOT_X, LEVER_PIVOT_Y);
        }
        float progress = unit.prevFlipProgress() + (unit.flipProgress() - unit.prevFlipProgress()) * partialTick;
        return new LeverRenderPlan((float) (LEVER_ROTATION_DEGREES * clamp(progress, 0.0D, 1.0D)),
                LEVER_PIVOT_X, LEVER_PIVOT_Y);
    }

    public static NumitronRenderPlan numitronRenderPlan(RBMKPanelPlanner.NumitronUnit unit) {
        String value = numitronValue(unit);
        long activeDigits = unit == null ? 0L : unit.activeDigits();
        List<DigitQuad> quads = new ArrayList<>();
        for (int i = 0; i < NUMITRON_DIGITS; i++) {
            if ((activeDigits & (NUMITRON_LEFT_DIGIT_MASK >> i)) == 0L) {
                continue;
            }
            DigitUv uv = digitUv(value.charAt(i));
            if (uv.blank()) {
                continue;
            }
            double zOffset = (i - 3) * NUMITRON_DIGIT_Z_STEP;
            quads.add(new DigitQuad(i, value.charAt(i), NUMITRON_DIGIT_X, NUMITRON_DIGIT_Y,
                    NUMITRON_DIGIT_WIDTH - zOffset, -NUMITRON_DIGIT_WIDTH - zOffset, uv));
        }
        return new NumitronRenderPlan(value, List.copyOf(quads));
    }

    public static TerminalRenderPlan terminalRenderPlan(String workingLine, RBMKPanelPlanner.TerminalState state,
            boolean blink) {
        RBMKPanelPlanner.TerminalState safe = state == null ? RBMKPanelPlanner.TerminalState.empty() : state;
        List<TerminalLine> lines = new ArrayList<>();
        String suffix = blink ? "_" : "";
        for (int i = 0; i < TERMINAL_RENDER_LINES; i++) {
            String label = i == 0 ? safeString(workingLine) : safe.history()[i - 1];
            boolean hasPrefix = i == 0 || !safeString(label).isEmpty();
            lines.add(new TerminalLine(i, hasPrefix ? "> " + safeString(label) : "", i == 0 ? suffix : ""));
        }
        int color = safe.repeatCommand().isEmpty() ? TERMINAL_NORMAL_COLOR : TERMINAL_REPEAT_COLOR;
        return new TerminalRenderPlan(TERMINAL_TEXT_X, TERMINAL_TEXT_Y, TERMINAL_TEXT_Z, TERMINAL_TEXT_SCALE,
                TERMINAL_MAX_TEXT_WIDTH, color, List.copyOf(lines));
    }

    public static DisplayRenderPlan displayRenderPlan(List<DisplayColumnInput> columns) {
        List<DisplayColumnPlan> plans = new ArrayList<>();
        int count = columns == null ? 0 : Math.min(columns.size(), RBMKPanelPlanner.DISPLAY_COLUMN_COUNT);
        for (int i = 0; i < count; i++) {
            DisplayColumnInput column = columns.get(i);
            if (column == null || column.type() == null) {
                continue;
            }
            double y = -(i / RBMKPanelPlanner.DISPLAY_GRID_SIZE) * 0.125D + 0.875D;
            double z = -(i % RBMKPanelPlanner.DISPLAY_GRID_SIZE) * 0.125D + 0.125D * 3.0D;
            int baseColor = column.indicator() > 0 ? DISPLAY_INDICATOR_COLOR : displayBaseColor(i, column);
            DotPlan dot = displayDot(column);
            plans.add(new DisplayColumnPlan(i, DISPLAY_COLUMN_X, y, z, baseColor, dot));
        }
        return new DisplayRenderPlan(DISPLAY_Y_TRANSLATE, DISPLAY_SCALE_YZ, List.copyOf(plans));
    }

    public static String numitronValue(RBMKPanelPlanner.NumitronUnit unit) {
        if (unit == null) {
            return "       ";
        }
        String value;
        if (unit.shortenNumber()) {
            value = shortNumber(unit.value());
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

    private static UnitTransform twoByTwoTransform(int index) {
        return new UnitTransform(PANEL_X, (index / 2) * TWO_BY_TWO_ROW_STEP + TWO_BY_TWO_Y_START,
                (index % 2) * TWO_BY_TWO_COLUMN_STEP + TWO_BY_TWO_Z_START);
    }

    private static GraphPoint graphPoint(int index, long value, long lowest, long range, int valueCount) {
        double y = GRAPH_BASE_Y + (value - lowest) * GRAPH_HEIGHT / Math.max(range, 1L);
        double z = GRAPH_Z_START - index * GRAPH_Z_SPAN / Math.max(valueCount - 1, 1);
        return new GraphPoint(GRAPH_LINE_X, y, z);
    }

    private static long legacyGraphClamp(long value, long lowest, long highest) {
        long result = value;
        if (result < lowest) {
            result = lowest;
        }
        if (result > highest) {
            result = highest;
        }
        return result;
    }

    private static int displayBaseColor(int index, DisplayColumnInput column) {
        if (column.color() >= 0) {
            return switch (column.color()) {
                case 0 -> 0xff0000;
                case 1 -> 0xffff00;
                case 2 -> 0x008000;
                case 3 -> 0x0000ff;
                case 4 -> 0x8000ff;
                default -> 0xffffff;
            };
        }
        double heat = column.maxHeat() == 0.0D ? 0.0D : column.heat() / column.maxHeat();
        heat = clamp(heat, 0.0D, 1.0D);
        double base = 0.65D + (index % 2) * 0.05D;
        int red = (int) Math.round((base + ((1.0D - base) * heat)) * 255.0D);
        int greenBlue = (int) Math.round(base * 255.0D);
        return red << 16 | greenBlue << 8 | greenBlue;
    }

    private static DotPlan displayDot(DisplayColumnInput column) {
        return switch (column.type()) {
            case FUEL, FUEL_SIM -> new DotPlan(true, 0x00ff00,
                    0.0F, (float) (0.25D + column.enrichment() * 0.75D), 0.0F);
            case CONTROL -> new DotPlan(true, 0xffff00,
                    (float) column.level(), (float) column.level(), 0.0F);
            case CONTROL_AUTO -> new DotPlan(true, 0xff00ff,
                    (float) column.level(), 0.0F, (float) column.level());
            default -> DotPlan.none();
        };
    }

    private static int scaleColor(int color, float multiplier) {
        int red = Math.round(((color >> 16) & 0xff) * multiplier);
        int green = Math.round(((color >> 8) & 0xff) * multiplier);
        int blue = Math.round((color & 0xff) * multiplier);
        return clamp(red, 0, 255) << 16 | clamp(green, 0, 255) << 8 | clamp(blue, 0, 255);
    }

    private static String shortNumber(long value) {
        double result;
        String suffix;
        long abs = Math.abs(value);
        if (abs >= Math.pow(10, 18)) {
            result = value / Math.pow(10, 18);
            suffix = "E";
        } else if (abs >= Math.pow(10, 15)) {
            result = value / Math.pow(10, 15);
            suffix = "P";
        } else if (abs >= Math.pow(10, 12)) {
            result = value / Math.pow(10, 12);
            suffix = "T";
        } else if (abs >= Math.pow(10, 9)) {
            result = value / Math.pow(10, 9);
            suffix = "G";
        } else if (abs >= Math.pow(10, 6)) {
            result = value / Math.pow(10, 6);
            suffix = "M";
        } else if (abs >= Math.pow(10, 3)) {
            result = value / Math.pow(10, 3);
            suffix = "k";
        } else {
            return Long.toString(value);
        }
        if (Math.abs(result) >= 100.0D) {
            return String.format(java.util.Locale.ROOT, "%.0f%s", result, suffix);
        }
        if (Math.abs(result) >= 10.0D) {
            return String.format(java.util.Locale.ROOT, "%.1f%s", result, suffix);
        }
        return String.format(java.util.Locale.ROOT, "%.2f%s", result, suffix);
    }

    private static long min(long[] values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        long min = Long.MAX_VALUE;
        for (long value : values) {
            min = Math.min(min, value);
        }
        return min;
    }

    private static long max(long[] values) {
        if (values == null || values.length == 0) {
            return 1L;
        }
        long max = Long.MIN_VALUE;
        for (long value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

    private static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    public record BaseTransform(
            double translateX,
            double translateY,
            double translateZ,
            double yawDegrees,
            boolean enableCullFace,
            boolean enableLighting) {
    }

    public record PanelRenderContract(
            RBMKPanelPlanner.PanelType type,
            String model,
            String texture,
            List<String> parts,
            int unitCount,
            boolean supportsLabels) {
    }

    public record UnitTransform(double x, double y, double z) {
    }

    public record GaugeNeedlePlan(
            double interpolatedValue,
            double rotationDegrees,
            double pivotY,
            double pivotZ,
            boolean renderNeedle) {
    }

    public record GraphPoint(double x, double y, double z) {
    }

    public record GraphSegment(GraphPoint start, GraphPoint end) {
    }

    public record GraphRenderPlan(long lowest, long highest, List<GraphSegment> segments) {
    }

    public record IndicatorLightPlan(int rgb, boolean fullBright, float multiplier) {
    }

    public record KeyButtonPlan(int rgb, double pressedXOffset, boolean fullBright, float multiplier) {
    }

    public record LeverRenderPlan(float rotationDegrees, double pivotX, double pivotY) {
    }

    public record DigitUv(double u, double v, boolean blank) {
    }

    public record DigitQuad(
            int digitIndex,
            char character,
            double x,
            double y,
            double zPositive,
            double zNegative,
            DigitUv uv) {
    }

    public record NumitronRenderPlan(String value, List<DigitQuad> digits) {
    }

    public record TerminalLine(int index, String text, String suffix) {
    }

    public record TerminalRenderPlan(
            double textX,
            double textY,
            double textZ,
            float scale,
            int maxWidth,
            int textColor,
            List<TerminalLine> lines) {
    }

    public record DisplayColumnInput(
            RBMKConsolePlanner.ColumnType type,
            int color,
            int indicator,
            double heat,
            double maxHeat,
            double enrichment,
            double level) {
    }

    public record DotPlan(boolean present, int rgb, float red, float green, float blue) {
        public static DotPlan none() {
            return new DotPlan(false, 0xffffff, 1.0F, 1.0F, 1.0F);
        }
    }

    public record DisplayColumnPlan(int index, double x, double y, double z, int baseColor, DotPlan dot) {
    }

    public record DisplayRenderPlan(double yTranslate, double yzScale, List<DisplayColumnPlan> columns) {
    }
}
