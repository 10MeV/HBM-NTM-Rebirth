package com.hbm.ntm.util;

public final class HbmClock {
    private static long timeMs;

    private HbmClock() {
    }

    public static void update() {
        timeMs = System.currentTimeMillis();
    }

    public static long getMs() {
        return timeMs;
    }
}
