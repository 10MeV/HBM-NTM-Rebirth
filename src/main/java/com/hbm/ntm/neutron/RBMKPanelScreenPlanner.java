package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKPanelScreenPlanner {
    public static final int CONFIG_WIDTH = 256;
    public static final int CONFIG_HEIGHT_SMALL = 150;
    public static final int CONFIG_HEIGHT_TALL = 204;
    public static final int INDICATOR_HEIGHT = 258;
    public static final int TEXT_FIELD_COLOR = 0x00ff00;
    public static final int TITLE_COLOR = 0x404040;
    public static final int TEXT_FIELD_OFFSET_X = 4;
    public static final int TEXT_FIELD_OFFSET_Y = 4;
    public static final int SAVE_BUTTON_X = 209;
    public static final int SAVE_BUTTON_Y = 17;
    public static final int SAVE_BUTTON_SIZE = 18;
    public static final int TERMINAL_LINE_MAX_LENGTH = 50;

    private RBMKPanelScreenPlanner() {
    }

    public static PanelScreenContract screenContract(RBMKPanelPlanner.PanelType type) {
        RBMKPanelPlanner.PanelType safeType = type == null ? RBMKPanelPlanner.PanelType.GAUGE : type;
        return switch (safeType) {
            case GAUGE -> configScreen(safeType, "container.rbmkGauge",
                    "hbm:textures/gui/machine/gui_rbmk_gauge.png", CONFIG_HEIGHT_TALL, true);
            case GRAPH -> configScreen(safeType, "container.rbmkGraph",
                    "hbm:textures/gui/machine/gui_rbmk_graph.png", CONFIG_HEIGHT_SMALL, true);
            case INDICATOR -> configScreen(safeType, "container.rbmkIndicator",
                    "hbm:textures/gui/machine/gui_rbmk_indicator.png", INDICATOR_HEIGHT, true);
            case KEYPAD -> configScreen(safeType, "container.rbmkKeyPad",
                    "hbm:textures/gui/machine/gui_rbmk_keypad.png", CONFIG_HEIGHT_TALL, true);
            case LEVER -> configScreen(safeType, "container.rbmkLever",
                    "hbm:textures/gui/machine/gui_rbmk_lever.png", CONFIG_HEIGHT_SMALL, true);
            case NUMITRON -> configScreen(safeType, "container.rbmkNumitron",
                    "hbm:textures/gui/machine/gui_rbmk_numitron.png", CONFIG_HEIGHT_SMALL, true);
            case TERMINAL -> new PanelScreenContract(
                    safeType,
                    "",
                    "",
                    0,
                    0,
                    false,
                    false,
                    true);
            case DISPLAY -> new PanelScreenContract(safeType, "", "", 0, 0, false, false, false);
        };
    }

    public static List<TextFieldPlan> textFields(RBMKPanelPlanner.PanelType type) {
        RBMKPanelPlanner.PanelType safeType = type == null ? RBMKPanelPlanner.PanelType.GAUGE : type;
        List<TextFieldPlan> fields = new ArrayList<>();
        switch (safeType) {
            case GAUGE -> {
                for (int i = 0; i < RBMKPanelPlanner.GAUGE_COUNT; i++) {
                    fields.add(rowField(i, "color", "color" + i, 27, 55, 72, 6, 36));
                    fields.add(rowField(i, "label", "label" + i, 175, 55, 72, 15, 36));
                    fields.add(rowField(i, "rtty", "rtty" + i, 27, 73, 72, 10, 36));
                    fields.add(rowField(i, "min", "min" + i, 121, 73, 52, 32, 36));
                    fields.add(rowField(i, "max", "max" + i, 195, 73, 52, 32, 36));
                }
            }
            case GRAPH -> {
                for (int i = 0; i < RBMKPanelPlanner.GRAPH_COUNT; i++) {
                    fields.add(rowField(i, "rtty", "rtty" + i, 27, 55, 72, 10, 54));
                    fields.add(rowField(i, "label", "label" + i, 27, 73, 72, 30, 54));
                    fields.add(rowField(i, "min", "min" + i, 175, 55, 72, 15, 54));
                    fields.add(rowField(i, "max", "max" + i, 175, 73, 72, 15, 54));
                }
            }
            case INDICATOR -> {
                for (int i = 0; i < RBMKPanelPlanner.INDICATOR_COUNT; i++) {
                    fields.add(rowField(i, "color", "color" + i, 27, 37, 72, 6, 36));
                    fields.add(rowField(i, "label", "label" + i, 175, 37, 72, 15, 36));
                    fields.add(rowField(i, "rtty", "rtty" + i, 27, 55, 72, 10, 36));
                    fields.add(rowField(i, "min", "min" + i, 121, 55, 52, 32, 36));
                    fields.add(rowField(i, "max", "max" + i, 195, 55, 52, 32, 36));
                }
            }
            case KEYPAD -> {
                for (int i = 0; i < RBMKPanelPlanner.KEY_COUNT; i++) {
                    fields.add(rowField(i, "color", "color" + i, 27, 55, 72, 6, 36));
                    fields.add(rowField(i, "label", "label" + i, 175, 55, 72, 15, 36));
                    fields.add(rowField(i, "rtty", "rtty" + i, 27, 73, 72, 10, 36));
                    fields.add(rowField(i, "command", "cmd" + i, 121, 73, 126, 32, 36));
                }
            }
            case LEVER -> {
                for (int i = 0; i < RBMKPanelPlanner.LEVER_COUNT; i++) {
                    fields.add(rowField(i, "rtty", "rtty" + i, 27, 55, 72, 10, 54));
                    fields.add(rowField(i, "label", "label" + i, 175, 55, 72, 15, 54));
                    fields.add(rowField(i, "commandOn", "cmdOn" + i, 45, 73, 81, 32, 54));
                    fields.add(rowField(i, "commandOff", "cmdOff" + i, 166, 73, 81, 32, 54));
                }
            }
            case NUMITRON -> {
                for (int i = 0; i < RBMKPanelPlanner.NUMITRON_COUNT; i++) {
                    fields.add(rowField(i, "rtty", "rtty" + i, 27, 55, 85, 10, 54));
                    fields.add(rowField(i, "label", "label" + i, 27, 73, 85, 30, 54));
                }
            }
            case TERMINAL -> fields.add(new TextFieldPlan(
                    0,
                    "line",
                    "cmd",
                    0,
                    0,
                    0,
                    0,
                    TERMINAL_LINE_MAX_LENGTH,
                    TEXT_FIELD_COLOR,
                    false));
            case DISPLAY -> {
            }
        }
        return List.copyOf(fields);
    }

    public static List<TogglePlan> toggles(RBMKPanelPlanner.PanelType type) {
        RBMKPanelPlanner.PanelType safeType = type == null ? RBMKPanelPlanner.PanelType.GAUGE : type;
        return switch (safeType) {
            case GAUGE, KEYPAD -> activePollingToggles(4, 111, 54, 128, 53, 36, CONFIG_HEIGHT_TALL);
            case GRAPH, LEVER -> activePollingToggles(2, 111, 54, 128, 53, 54, CONFIG_HEIGHT_SMALL);
            case INDICATOR -> activePollingToggles(6, 111, 36, 128, 35, 36, CONFIG_HEIGHT_SMALL);
            case NUMITRON -> numitronToggles();
            case TERMINAL, DISPLAY -> List.of();
        };
    }

    public static List<TextureRect> staticTextureRects(RBMKPanelPlanner.PanelType type) {
        RBMKPanelPlanner.PanelType safeType = type == null ? RBMKPanelPlanner.PanelType.GAUGE : type;
        List<TextureRect> rects = new ArrayList<>();
        if (safeType == RBMKPanelPlanner.PanelType.INDICATOR) {
            rects.add(new TextureRect("top_background", 0, 0, 0, 0, 256, 143));
            rects.add(new TextureRect("bottom_background", 0, 143, 0, 35, 256, 115));
            for (int i = 0; i < RBMKPanelPlanner.INDICATOR_COUNT; i++) {
                rects.add(new TextureRect("indicator_color_swatch_" + i, 102, 148 + i * 36, 34 + 8 * i,
                        150, 8, 8));
            }
            return List.copyOf(rects);
        }
        PanelScreenContract contract = screenContract(safeType);
        if (contract.hasConfigScreen() && !contract.texture().isEmpty()) {
            rects.add(new TextureRect("background", 0, 0, 0, 0, contract.width(), contract.height()));
        }
        return List.copyOf(rects);
    }

    public static SaveButtonPlan saveButton(RBMKPanelPlanner.PanelType type) {
        RBMKPanelPlanner.PanelType safeType = type == null ? RBMKPanelPlanner.PanelType.GAUGE : type;
        if (safeType == RBMKPanelPlanner.PanelType.TERMINAL || safeType == RBMKPanelPlanner.PanelType.DISPLAY) {
            return SaveButtonPlan.none();
        }
        int y = safeType == RBMKPanelPlanner.PanelType.INDICATOR ? 8 : SAVE_BUTTON_Y;
        return new SaveButtonPlan(true, SAVE_BUTTON_X, y, SAVE_BUTTON_SIZE, SAVE_BUTTON_SIZE, "active", "polling",
                extraMaskKeys(safeType));
    }

    public static TerminalScreenPlan terminalScreenPlan(boolean terminalInvalid) {
        return new TerminalScreenPlan(
                terminalInvalid,
                TERMINAL_LINE_MAX_LENGTH,
                true,
                List.of(199, 203, 205, 207),
                "cmd",
                List.of(
                        new HelpLine("[Esc] - Quit", 2, 2),
                        new HelpLine("chan <channel> - Set selected channel", 2, 12),
                        new HelpLine("send <cmd> - Send single signal over selected channel", 2, 22),
                        new HelpLine("start <cmd> - Continuously send signal over selected channel", 2, 32),
                        new HelpLine("stop - Stop continuous sending", 2, 42),
                        new HelpLine("clear - Delete command history", 2, 52)));
    }

    private static PanelScreenContract configScreen(RBMKPanelPlanner.PanelType type, String titleKey, String texture,
            int height, boolean repeatKeyboard) {
        return new PanelScreenContract(type, titleKey, texture, CONFIG_WIDTH, height, repeatKeyboard, false, true);
    }

    private static TextFieldPlan rowField(int unit, String name, String packetKey, int x, int y, int width,
            int maxLength, int rowStep) {
        return new TextFieldPlan(unit, name, packetKey, x + TEXT_FIELD_OFFSET_X, y + TEXT_FIELD_OFFSET_Y
                + unit * rowStep, width - TEXT_FIELD_OFFSET_X * 2, 14, maxLength, TEXT_FIELD_COLOR, false);
    }

    private static List<TogglePlan> activePollingToggles(int count, int activeX, int activeY, int pollingX,
            int pollingY, int rowStep, int textureV) {
        List<TogglePlan> plans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            plans.add(new TogglePlan(i, "active", activeX, activeY + i * rowStep, 16, 16, 18, textureV, 16, 16));
            plans.add(new TogglePlan(i, "polling", pollingX, pollingY + i * rowStep, 18, 18, 0, textureV, 18, 18));
        }
        return List.copyOf(plans);
    }

    private static List<TogglePlan> numitronToggles() {
        List<TogglePlan> plans = new ArrayList<>();
        for (int i = 0; i < RBMKPanelPlanner.NUMITRON_COUNT; i++) {
            plans.add(new TogglePlan(i, "active", 124, 54 + i * 54, 16, 16, 18, 150, 16, 16));
            plans.add(new TogglePlan(i, "polling", 159, 53 + i * 54, 18, 18, 0, 150, 18, 18));
            plans.add(new TogglePlan(i, "shorten_number", 195, 53 + i * 54, 18, 18, 34, 150, 18, 18));
            plans.add(new TogglePlan(i, "leading_zeroes", 231, 53 + i * 54, 18, 18, 52, 150, 18, 18));
        }
        return List.copyOf(plans);
    }

    private static List<String> extraMaskKeys(RBMKPanelPlanner.PanelType type) {
        if (type == RBMKPanelPlanner.PanelType.NUMITRON) {
            return List.of("shorten_number", "leading_zeroes");
        }
        return List.of();
    }

    public record PanelScreenContract(
            RBMKPanelPlanner.PanelType type,
            String titleKey,
            String texture,
            int width,
            int height,
            boolean enableRepeatEvents,
            boolean pausesGame,
            boolean hasConfigScreen) {
    }

    public record TextFieldPlan(
            int unit,
            String name,
            String packetKey,
            int x,
            int y,
            int width,
            int height,
            int maxLength,
            int textColor,
            boolean backgroundDrawing) {
    }

    public record TogglePlan(
            int unit,
            String maskKey,
            int x,
            int y,
            int width,
            int height,
            int textureU,
            int textureV,
            int textureWidth,
            int textureHeight) {
    }

    public record TextureRect(String name, int x, int y, int u, int v, int width, int height) {
    }

    public record SaveButtonPlan(
            boolean present,
            int x,
            int y,
            int width,
            int height,
            String activeMaskKey,
            String pollingMaskKey,
            List<String> extraMaskKeys) {
        public static SaveButtonPlan none() {
            return new SaveButtonPlan(false, 0, 0, 0, 0, "", "", List.of());
        }
    }

    public record TerminalScreenPlan(
            boolean closeBecauseTerminalInvalid,
            int lineMaxLength,
            boolean focusLineOnInit,
            List<Integer> ignoredKeyCodes,
            String packetKey,
            List<HelpLine> helpLines) {
    }

    public record HelpLine(String text, int x, int y) {
    }
}
