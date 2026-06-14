package com.hbm.ntm.util;

import java.util.Collections;
import java.util.Map;

/**
 * Legacy-name facade for the 1.7.10 stat id repair hook.
 */
@Deprecated(forRemoval = false)
public final class StatHelper {
    public static final Map<?, ?> publicReferenceToOneshotStatListPleaseAllPointAndLaugh = Collections.emptyMap();

    private StatHelper() {
    }

    public static void resetStatShitFuck() {
        // 1.20.1 stats are registry-backed and do not use the old numeric id arrays repaired by this hook.
    }

    public static boolean isLegacyStatResetRequired() {
        return false;
    }
}
