package com.hbm.ntm.util;

/**
 * Legacy-name logging facade.
 */
@Deprecated(forRemoval = false)
public final class LoggingUtil {
    public static final String ANSI_RESET = HbmLoggingUtil.ANSI_RESET;
    public static final String ANSI_BLACK = HbmLoggingUtil.ANSI_BLACK;
    public static final String ANSI_RED = HbmLoggingUtil.ANSI_RED;
    public static final String ANSI_GREEN = HbmLoggingUtil.ANSI_GREEN;
    public static final String ANSI_YELLOW = HbmLoggingUtil.ANSI_YELLOW;
    public static final String ANSI_BLUE = HbmLoggingUtil.ANSI_BLUE;
    public static final String ANSI_PURPLE = HbmLoggingUtil.ANSI_PURPLE;
    public static final String ANSI_CYAN = HbmLoggingUtil.ANSI_CYAN;
    public static final String ANSI_WHITE = HbmLoggingUtil.ANSI_WHITE;

    private LoggingUtil() {
    }

    public static void errorWithHighlight(String error) {
        HbmLoggingUtil.errorWithHighlight(error);
    }
}
