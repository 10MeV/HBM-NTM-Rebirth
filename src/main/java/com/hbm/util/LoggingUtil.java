package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for logging helpers.
 */
@Deprecated(forRemoval = false)
public final class LoggingUtil {
    public static final String ANSI_RESET = com.hbm.ntm.util.LoggingUtil.ANSI_RESET;
    public static final String ANSI_BLACK = com.hbm.ntm.util.LoggingUtil.ANSI_BLACK;
    public static final String ANSI_RED = com.hbm.ntm.util.LoggingUtil.ANSI_RED;
    public static final String ANSI_GREEN = com.hbm.ntm.util.LoggingUtil.ANSI_GREEN;
    public static final String ANSI_YELLOW = com.hbm.ntm.util.LoggingUtil.ANSI_YELLOW;
    public static final String ANSI_BLUE = com.hbm.ntm.util.LoggingUtil.ANSI_BLUE;
    public static final String ANSI_PURPLE = com.hbm.ntm.util.LoggingUtil.ANSI_PURPLE;
    public static final String ANSI_CYAN = com.hbm.ntm.util.LoggingUtil.ANSI_CYAN;
    public static final String ANSI_WHITE = com.hbm.ntm.util.LoggingUtil.ANSI_WHITE;

    private LoggingUtil() {
    }

    public static void errorWithHighlight(String error) {
        com.hbm.ntm.util.LoggingUtil.errorWithHighlight(error);
    }
}
