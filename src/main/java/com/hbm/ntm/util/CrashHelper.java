package com.hbm.ntm.util;

import com.hbm.ntm.compat.CompatRecipeRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraftforge.fml.CrashReportCallables;

/**
 * Modern crash-report companion for legacy common helpers.
 */
public final class CrashHelper {
    private static final String MODIFIED_RECIPES_LABEL = "NTM Modified recipes";
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

    private CrashHelper() {
    }

    public static void init() {
        if (INITIALIZED.compareAndSet(false, true)) {
            CrashReportCallables.registerCrashCallable(MODIFIED_RECIPES_LABEL, CrashHelper::modifiedRecipeSummary);
        }
    }

    public static String modifiedRecipeSummary() {
        return CompatRecipeRegistry.diagnostics().summary();
    }
}
