package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for crash diagnostics.
 */
@Deprecated(forRemoval = false)
public final class CrashHelper {
    private CrashHelper() {
    }

    public static void init() {
        com.hbm.ntm.util.CrashHelper.init();
    }

    public static String modifiedRecipeSummary() {
        return com.hbm.ntm.util.CrashHelper.modifiedRecipeSummary();
    }

    public static class CrashCallableRecipe {
        public String getLabel() {
            return "NTM Modified recipes";
        }

        public String call() {
            return modifiedRecipeSummary();
        }
    }
}
