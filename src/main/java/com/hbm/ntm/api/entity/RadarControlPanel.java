package com.hbm.ntm.api.entity;

import java.util.List;

public final class RadarControlPanel {
    public static final int BUTTON_SIZE = 8;
    public static final int MAIN_BUTTON_X = -10;
    public static final int MAIN_BUTTON_START_Y = 88;
    public static final int MAIN_BUTTON_STEP_Y = 10;
    public static final int MAIN_TOGGLE_VIEW_Y = 158;
    public static final int MAIN_CLEAR_MAP_Y = 178;
    public static final int SLOT_TOGGLE_START_X = 52;
    public static final int SLOT_TOGGLE_Y = 82;
    public static final int SLOT_TOGGLE_STEP_X = 12;
    public static final int ICON_U = 238;
    public static final int ICON_START_V = 4;
    public static final int ICON_STEP_V = 10;

    private static final List<Button> BUTTONS = List.of(
            new Button(0, RadarControl.SCAN_MISSILES, "radar.detectMissiles"),
            new Button(1, RadarControl.SCAN_SHELLS, "radar.detectShells"),
            new Button(2, RadarControl.SCAN_PLAYERS, "radar.detectPlayers"),
            new Button(3, RadarControl.SMART_MODE, "radar.smartMode"),
            new Button(4, RadarControl.REDSTONE_MODE, "radar.redMode"),
            new Button(5, RadarControl.SHOW_MAP, "radar.showMap"));

    private RadarControlPanel() {
    }

    public static List<Button> buttons() {
        return BUTTONS;
    }

    public static int mainToggleViewX() {
        return MAIN_BUTTON_X;
    }

    public static int mainClearMapX() {
        return MAIN_BUTTON_X;
    }

    public record Button(int index, RadarControl control, String tooltipKey) {
        public int mainX() {
            return MAIN_BUTTON_X;
        }

        public int mainY() {
            return MAIN_BUTTON_START_Y + index * MAIN_BUTTON_STEP_Y;
        }

        public int slotX() {
            return SLOT_TOGGLE_START_X + index * SLOT_TOGGLE_STEP_X;
        }

        public int slotY() {
            return SLOT_TOGGLE_Y;
        }

        public int iconU() {
            return ICON_U;
        }

        public int iconV() {
            return ICON_START_V + index * ICON_STEP_V;
        }
    }
}
