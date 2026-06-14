package com.hbm.util;

import java.util.Map;

/**
 * Legacy 1.7.10 package bridge for the old stat id repair hook.
 */
@Deprecated(forRemoval = false)
public final class StatHelper {
    public static final Map<?, ?> publicReferenceToOneshotStatListPleaseAllPointAndLaugh =
            com.hbm.ntm.util.StatHelper.publicReferenceToOneshotStatListPleaseAllPointAndLaugh;

    private StatHelper() {
    }

    public static void resetStatShitFuck() {
        com.hbm.ntm.util.StatHelper.resetStatShitFuck();
    }

    public static boolean isLegacyStatResetRequired() {
        return com.hbm.ntm.util.StatHelper.isLegacyStatResetRequired();
    }
}
