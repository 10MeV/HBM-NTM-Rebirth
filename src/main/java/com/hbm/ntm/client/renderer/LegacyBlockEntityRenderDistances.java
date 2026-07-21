package com.hbm.ntm.client.renderer;

import com.hbm.ntm.util.HbmModelRenderDistances;

public final class LegacyBlockEntityRenderDistances {
    /**
     * @deprecated Use {@link #machine()} so every block-entity model shares the global contract.
     */
    @Deprecated
    public static final int MACHINE = HbmModelRenderDistances.BLOCKS;
    private LegacyBlockEntityRenderDistances() {
    }

    public static int machine() {
        return MACHINE;
    }
}
