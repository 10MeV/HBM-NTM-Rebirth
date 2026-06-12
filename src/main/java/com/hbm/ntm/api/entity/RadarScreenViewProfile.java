package com.hbm.ntm.api.entity;

public record RadarScreenViewProfile(boolean main, int width, int height) {
    public static final int LABEL_COLOR = 0x404040;
    public static final int TITLE_Y = 6;
    public static final int INVENTORY_LABEL_X = 8;

    public static final RadarScreenViewProfile MAIN =
            new RadarScreenViewProfile(true, RadarGuiLayout.MAIN_WIDTH, RadarGuiLayout.MAIN_HEIGHT);
    public static final RadarScreenViewProfile SLOTS =
            new RadarScreenViewProfile(false, RadarGuiLayout.SLOT_WIDTH, RadarGuiLayout.SLOT_HEIGHT);

    public static RadarScreenViewProfile fromMainFlag(boolean main) {
        return main ? MAIN : SLOTS;
    }

    public int inventoryLabelY() {
        return height - 96 + 2;
    }

    public int inventoryLabelX() {
        return INVENTORY_LABEL_X;
    }

    public int titleX(int titleWidth) {
        return width / 2 - titleWidth / 2;
    }

    public int titleY() {
        return TITLE_Y;
    }

    public int labelColor() {
        return LABEL_COLOR;
    }
}
