package com.hbm.ntm.api.entity;

public final class RadarLaunchKeyProfile {
    private static final int KEY_1 = 49;
    private static final int KEY_8 = 56;
    private static final int KEY_KP_1 = 321;
    private static final int KEY_KP_8 = 328;

    private RadarLaunchKeyProfile() {
    }

    public static int linkSlotForKey(int keyCode) {
        if (keyCode >= KEY_1 && keyCode <= KEY_8) {
            return keyCode - KEY_1;
        }
        if (keyCode >= KEY_KP_1 && keyCode <= KEY_KP_8) {
            return keyCode - KEY_KP_1;
        }
        return -1;
    }

    public static boolean isLaunchKey(int keyCode) {
        return linkSlotForKey(keyCode) >= 0;
    }
}
