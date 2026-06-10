package com.hbm.ntm.api.entity;

public final class RadarGuiHitProfile {
    private static final int BLIP_MOUSE_MIN_OFFSET = -4;
    private static final int BLIP_MOUSE_MAX_OFFSET = 5;

    private RadarGuiHitProfile() {
    }

    public static boolean hitsBlip(int mouseX, int mouseY, int blipX, int blipY) {
        return mouseX + BLIP_MOUSE_MAX_OFFSET > blipX
                && mouseX + BLIP_MOUSE_MIN_OFFSET <= blipX
                && mouseY + BLIP_MOUSE_MAX_OFFSET > blipY
                && mouseY + BLIP_MOUSE_MIN_OFFSET <= blipY;
    }

    public static boolean hitsArea(int leftPos, int topPos, int relX, int relY, int width, int height,
            double mouseX, double mouseY) {
        return leftPos + relX <= mouseX
                && leftPos + relX + width > mouseX
                && topPos + relY < mouseY
                && topPos + relY + height >= mouseY;
    }

    public static boolean hitsRadarArea(int leftPos, int topPos, double mouseX, double mouseY) {
        return hitsArea(leftPos, topPos, RadarGuiLayout.RADAR_AREA_X, RadarGuiLayout.RADAR_AREA_Y,
                RadarGuiLayout.RADAR_AREA_SIZE, RadarGuiLayout.RADAR_AREA_SIZE, mouseX, mouseY);
    }
}
