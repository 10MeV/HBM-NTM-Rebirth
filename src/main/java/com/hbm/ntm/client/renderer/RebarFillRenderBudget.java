package com.hbm.ntm.client.renderer;

import com.hbm.ntm.config.HbmClientConfig;

/** Per-frame equivalent of the old loaded-tile rebar-fill truncation. */
public final class RebarFillRenderBudget {
    private static int remaining;

    private RebarFillRenderBudget() {
    }

    public static void beginFrame() {
        remaining = HbmClientConfig.renderRebarLimit();
    }

    public static boolean tryAcquire() {
        if (remaining <= 0) {
            return false;
        }
        remaining--;
        return true;
    }
}
