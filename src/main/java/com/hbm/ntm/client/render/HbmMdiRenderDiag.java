package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.HbmClientConfig;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HbmMdiRenderDiag {
    private static final AtomicBoolean BANNER_LOGGED = new AtomicBoolean();

    private HbmMdiRenderDiag() {
    }

    public static boolean isDispatchDebugEnabled() {
        return HbmClientConfig.mdiDebugLogDispatch();
    }

    public static boolean isVerboseSubdrawsEnabled() {
        return HbmClientConfig.mdiVerboseSubdraws();
    }

    public static boolean shouldLogDispatchSummary() {
        return HbmClientConfig.renderBackendDiagnostics()
                || isDispatchDebugEnabled()
                || isVerboseSubdrawsEnabled();
    }

    public static void logBannerOnce() {
        if (!isDispatchDebugEnabled() && !isVerboseSubdrawsEnabled()) {
            return;
        }
        if (BANNER_LOGGED.compareAndSet(false, true)) {
            HbmNtm.LOGGER.info(
                    "[HBM MDI] Diagnostics enabled: rendering.mdiDebugLogDispatch / rendering.mdiVerboseSubdraws.");
        }
    }
}
