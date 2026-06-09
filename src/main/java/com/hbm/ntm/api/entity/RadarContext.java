package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record RadarContext(
        ServerLevel level,
        BlockPos origin,
        int range,
        int verticalBuffer,
        int minimumAltitude,
        RadarDetectable.RadarScanParams params) {
    public static final int LEGACY_RANGE = 1_000;
    public static final int LEGACY_LARGE_RANGE = 3_000;
    public static final int LEGACY_VERTICAL_BUFFER = 30;
    public static final int LEGACY_MINIMUM_ALTITUDE = 55;

    public RadarContext {
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }
        if (origin == null) {
            throw new IllegalArgumentException("origin cannot be null");
        }
        if (params == null) {
            params = RadarDetectable.RadarScanParams.DEFAULT;
        }
        range = Math.max(0, range);
        verticalBuffer = Math.max(0, verticalBuffer);
    }

    public static RadarContext legacy(ServerLevel level, BlockPos origin) {
        return legacy(level, origin, LEGACY_RANGE, RadarDetectable.RadarScanParams.DEFAULT);
    }

    public static RadarContext legacy(ServerLevel level, BlockPos origin, int range,
            RadarDetectable.RadarScanParams params) {
        return new RadarContext(level, origin, range, LEGACY_VERTICAL_BUFFER, LEGACY_MINIMUM_ALTITUDE, params);
    }

    public static RadarContext legacy(ServerLevel level, BlockPos origin, int range, int verticalBuffer,
            int minimumAltitude, RadarDetectable.RadarScanParams params) {
        return new RadarContext(level, origin, range, verticalBuffer, minimumAltitude, params);
    }
}
