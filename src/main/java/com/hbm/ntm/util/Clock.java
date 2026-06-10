package com.hbm.ntm.util;

/**
 * Legacy-name facade for the cached millisecond clock.
 */
@Deprecated(forRemoval = false)
public final class Clock {
    private Clock() {
    }

    public static void update() {
        HbmClock.update();
    }

    public static long get_ms() {
        return HbmClock.getMs();
    }

    public static long getMs() {
        return HbmClock.getMs();
    }
}
