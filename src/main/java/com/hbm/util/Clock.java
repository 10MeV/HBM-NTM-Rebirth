package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for the cached millisecond clock.
 */
@Deprecated(forRemoval = false)
public final class Clock {
    private Clock() {
    }

    public static void update() {
        com.hbm.ntm.util.Clock.update();
    }

    public static long get_ms() {
        return com.hbm.ntm.util.Clock.get_ms();
    }

    public static long getMs() {
        return com.hbm.ntm.util.Clock.getMs();
    }
}
