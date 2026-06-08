package com.hbm.ntm.util;

import com.hbm.ntm.api.common.HalfLifeType;

public final class HbmRtgUtil {
    private static final long TICKS_PER_LEGACY_DAY = 48_000L;
    private static final long LEGACY_DAYS_PER_YEAR = 100L;
    private static final long REAL_DAYS_PER_YEAR = 365L;

    private HbmRtgUtil() {
    }

    public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
        long daysPerYear = realYears ? REAL_DAYS_PER_YEAR : LEGACY_DAYS_PER_YEAR;
        float scale = switch (type) {
            case LONG -> TICKS_PER_LEGACY_DAY * daysPerYear * LEGACY_DAYS_PER_YEAR;
            case MEDIUM -> TICKS_PER_LEGACY_DAY * daysPerYear;
            case SHORT -> TICKS_PER_LEGACY_DAY;
        };
        return (long) (scale * halfLife);
    }
}
