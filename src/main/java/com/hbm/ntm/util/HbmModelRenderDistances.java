package com.hbm.ntm.util;

/** Shared, source-independent modern render-distance contract for every model culling path. */
public final class HbmModelRenderDistances {
    public static final int BLOCKS = 512;
    public static final double SQUARED_BLOCKS = (double) BLOCKS * (double) BLOCKS;

    private HbmModelRenderDistances() {
    }

    public static boolean shouldRenderAtSqrDistance(double distanceSquared) {
        return distanceSquared < SQUARED_BLOCKS;
    }
}
