package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Arrays;

public record RadarMapScanPlan(boolean clearMap, boolean scanMap, RadarMapScanSlice scanSlice,
                               boolean clearFlagAfter, int lastMapSliceAfter, boolean changed) {
    public RadarMapScanPlan {
        if (!scanMap) {
            scanSlice = null;
        }
        lastMapSliceAfter = scanMap && scanSlice != null ? scanSlice.slice() : RadarMapUpdate.NO_SLICE;
    }

    public static RadarMapScanPlan forState(BlockPos origin, int range, long gameTime, boolean clearFlag,
            boolean showMap) {
        if (clearFlag) {
            return new RadarMapScanPlan(true, false, null, false, RadarMapUpdate.NO_SLICE, true);
        }
        if (!showMap) {
            return new RadarMapScanPlan(false, false, null, false, RadarMapUpdate.NO_SLICE, false);
        }
        RadarMapScanSlice scanSlice = RadarMapScanSlice.forGameTime(origin, range, gameTime);
        return new RadarMapScanPlan(false, true, scanSlice, false, scanSlice.slice(), true);
    }

    public byte[] applyClear(byte[] map) {
        byte[] normalized = RadarMap.normalize(map);
        if (clearMap) {
            Arrays.fill(normalized, (byte) 0);
        }
        return normalized;
    }

    public void executeScan(ServerLevel level, byte[] map, int chunkLoadCap, boolean generateChunks) {
        if (scanMap && scanSlice != null) {
            RadarMapScanOperation.execute(level, map, scanSlice, chunkLoadCap, generateChunks);
        }
    }

    public static RadarMapUpdate mapUpdateSnapshot(boolean mapClearDirty, boolean showMap, int lastMapSlice,
            byte[] map) {
        if (mapClearDirty) {
            return RadarMapUpdate.CLEAR;
        }
        if (!showMap || lastMapSlice < 0) {
            return RadarMapUpdate.NONE;
        }
        return RadarMapUpdate.sliceFromMap(lastMapSlice, map);
    }
}
