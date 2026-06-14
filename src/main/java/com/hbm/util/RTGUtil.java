package com.hbm.util;

import com.hbm.ntm.api.common.HalfLifeType;

/**
 * Legacy 1.7.10 package bridge for RTG lifespan helpers.
 */
@Deprecated(forRemoval = false)
public final class RTGUtil {
    private RTGUtil() {
    }

    public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
        return com.hbm.ntm.util.RTGUtil.getLifespan(halfLife, type, realYears);
    }
}
