package com.hbm.ntm.util;

import com.hbm.ntm.HbmNtm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HbmTimeAnalyzer {
    private static final List<Delta> DELTAS = new ArrayList<>();
    private static String currentSection = "";
    private static long sectionStartTime;

    private HbmTimeAnalyzer() {
    }

    public static void startCount(String section) {
        currentSection = section;
        sectionStartTime = System.nanoTime();
    }

    public static void endCount() {
        if (currentSection.isEmpty()) {
            return;
        }
        DELTAS.add(new Delta(currentSection, System.nanoTime() - sectionStartTime));
    }

    public static void startEndCount(String section) {
        endCount();
        startCount(section);
    }

    public static Map<String, Long> collect() {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (Delta delta : DELTAS) {
            totals.merge(delta.section(), delta.nanoseconds(), Long::sum);
        }
        return totals;
    }

    public static void dump() {
        Map<String, Long> totals = collect();
        long total = 0L;
        for (Map.Entry<String, Long> entry : totals.entrySet()) {
            total += entry.getValue();
            HbmNtm.LOGGER.info("{}: {}ns", entry.getKey(), format(entry.getValue()));
        }
        HbmNtm.LOGGER.info("Total time passed: {}ns ({}s)", format(total), total / 1_000_000_000L);
        reset();
    }

    public static void reset() {
        currentSection = "";
        sectionStartTime = 0L;
        DELTAS.clear();
    }

    private static String format(long value) {
        return String.format(Locale.US, "%,d", value);
    }

    private record Delta(String section, long nanoseconds) {
    }
}
