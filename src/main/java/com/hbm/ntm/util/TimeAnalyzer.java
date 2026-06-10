package com.hbm.ntm.util;

import java.util.Map;

/**
 * Legacy-name lightweight profiler facade.
 */
@Deprecated(forRemoval = false)
public final class TimeAnalyzer {
    private TimeAnalyzer() {
    }

    public static void startCount(String section) {
        HbmTimeAnalyzer.startCount(section);
    }

    public static void endCount() {
        HbmTimeAnalyzer.endCount();
    }

    public static void startEndCount(String section) {
        HbmTimeAnalyzer.startEndCount(section);
    }

    public static Map<String, Long> collect() {
        return HbmTimeAnalyzer.collect();
    }

    public static void dump() {
        HbmTimeAnalyzer.dump();
    }

    public static void reset() {
        HbmTimeAnalyzer.reset();
    }
}
