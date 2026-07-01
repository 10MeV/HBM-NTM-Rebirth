package com.hbm.ntm.client.renderer;

import com.hbm.ntm.config.HbmClientConfig;

public final class LegacyBlockEntityRenderDistances {
    /**
     * @deprecated Use {@link #machine()} so the Modernized-style client distance config is observed.
     */
    @Deprecated
    public static final int MACHINE = 32 * 16;
    public static final int LEGACY_65536_SQUARED = 256;

    private LegacyBlockEntityRenderDistances() {
    }

    public static int machine() {
        return HbmClientConfig.modelStaticRenderDistanceBlocks();
    }
}
