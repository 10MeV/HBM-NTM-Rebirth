package com.hbm.util;

import java.util.Map;

/**
 * Legacy 1.7.10 package bridge for lightweight profiling helpers.
 */
@Deprecated(forRemoval = false)
public final class TimeAnalyzer {
    private TimeAnalyzer() {
    }

    public static void startCount(String section) {
        com.hbm.ntm.util.TimeAnalyzer.startCount(section);
    }

    public static void endCount() {
        com.hbm.ntm.util.TimeAnalyzer.endCount();
    }

    public static void startEndCount(String section) {
        com.hbm.ntm.util.TimeAnalyzer.startEndCount(section);
    }

    public static Map<String, Long> collect() {
        return com.hbm.ntm.util.TimeAnalyzer.collect();
    }

    public static void dump() {
        com.hbm.ntm.util.TimeAnalyzer.dump();
    }

    public static void reset() {
        com.hbm.ntm.util.TimeAnalyzer.reset();
    }
}
