package com.hbm.ntm.util;

import com.hbm.ntm.compat.CompatRecipeRegistry;

/**
 * Modern crash-report companion for legacy common helpers.
 */
public final class CrashHelper {
    private CrashHelper() {
    }

    public static void init() {
    }

    public static String modifiedRecipeSummary() {
        return CompatRecipeRegistry.diagnostics().summary();
    }
}
