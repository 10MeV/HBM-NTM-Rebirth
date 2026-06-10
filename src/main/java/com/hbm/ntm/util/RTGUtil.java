package com.hbm.ntm.util;

import com.hbm.ntm.api.common.HalfLifeType;

/**
 * Legacy-name RTG utility facade.
 */
@Deprecated(forRemoval = false)
public final class RTGUtil {
    private RTGUtil() {
    }

    public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
        return HbmRtgUtil.getLifespan(halfLife, type, realYears);
    }
}
