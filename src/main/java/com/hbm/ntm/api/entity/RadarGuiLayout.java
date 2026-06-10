package com.hbm.ntm.api.entity;

public final class RadarGuiLayout {
    public static final int MAIN_WIDTH = 216;
    public static final int MAIN_HEIGHT = 234;
    public static final int SLOT_WIDTH = 176;
    public static final int SLOT_HEIGHT = 184;

    public static final int RADAR_AREA_X = 8;
    public static final int RADAR_AREA_Y = 17;
    public static final int RADAR_AREA_SIZE = 200;
    public static final int RADAR_CENTER_X = 108;
    public static final int RADAR_CENTER_Y = 117;
    public static final int MAP_PIXEL_OFFSET_Y = 1;

    public static final int MAIN_ENERGY_X = 8;
    public static final int MAIN_ENERGY_Y = 221;
    public static final int MAIN_ENERGY_WIDTH = 200;
    public static final int MAIN_ENERGY_HEIGHT = 7;
    public static final int MAIN_ENERGY_U = 0;
    public static final int MAIN_ENERGY_V = 234;
    public static final int MAIN_ENERGY_TEXTURE_HEIGHT = 16;

    public static final int SLOT_ENERGY_X = 8;
    public static final int SLOT_ENERGY_Y = 64;
    public static final int SLOT_ENERGY_WIDTH = 160;
    public static final int SLOT_ENERGY_HEIGHT = 16;
    public static final int SLOT_ENERGY_U = 0;
    public static final int SLOT_ENERGY_V = 185;

    public static final int SLOT_TOGGLE_VIEW_X = 5;
    public static final int SLOT_TOGGLE_VIEW_Y = 5;
    public static final int SLOT_TOGGLE_VIEW_SIZE = 8;

    public static final int MAIN_SIDE_STRIP_X = -14;
    public static final int MAIN_SIDE_STRIP_TOP_Y = 84;
    public static final int MAIN_SIDE_STRIP_BOTTOM_Y = 154;
    public static final int MAIN_SIDE_STRIP_U = 224;
    public static final int MAIN_SIDE_STRIP_TOP_V = 0;
    public static final int MAIN_SIDE_STRIP_BOTTOM_V = 66;
    public static final int MAIN_SIDE_STRIP_WIDTH = 14;
    public static final int MAIN_SIDE_STRIP_TOP_HEIGHT = 66;
    public static final int MAIN_SIDE_STRIP_BOTTOM_HEIGHT = 36;

    private RadarGuiLayout() {
    }

    public static int mapPixelX(int leftPos, int index) {
        return leftPos + RADAR_AREA_X + RadarMap.gridX(index);
    }

    public static int mapPixelY(int topPos, int index) {
        return topPos + RADAR_AREA_Y + MAP_PIXEL_OFFSET_Y + RadarMap.gridZ(index);
    }
}
