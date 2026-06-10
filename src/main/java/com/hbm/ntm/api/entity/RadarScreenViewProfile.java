package com.hbm.ntm.api.entity;

public record RadarScreenViewProfile(boolean main, int width, int height) {
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
}
