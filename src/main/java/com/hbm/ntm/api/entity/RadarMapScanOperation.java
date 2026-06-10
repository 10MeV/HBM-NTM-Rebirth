package com.hbm.ntm.api.entity;

import com.hbm.ntm.world.WorldUtil;
import net.minecraft.server.level.ServerLevel;

public final class RadarMapScanOperation {
    private RadarMapScanOperation() {
    }

    public static void execute(ServerLevel level, byte[] map, RadarMapScanSlice scanSlice, int chunkLoadCap,
            boolean generateChunks) {
        if (level == null || map == null || scanSlice == null || !scanSlice.valid()) {
            return;
        }
        int chunkLoads = 0;
        for (RadarMapScanSlice.Sample sample : scanSlice.samples()) {
            boolean chunkLoaded = level.hasChunk(sample.chunkX(), sample.chunkZ());
            if (!chunkLoaded && map[sample.index()] == 0 && chunkLoads < chunkLoadCap
                    && tryLoadChunk(level, sample.chunkX(), sample.chunkZ(), generateChunks)) {
                chunkLoads++;
                chunkLoaded = level.hasChunk(sample.chunkX(), sample.chunkZ());
            }
            if (chunkLoaded) {
                map[sample.index()] = RadarMap.sampleHeight(level, sample.x(), sample.z());
            }
        }
    }

    private static boolean tryLoadChunk(ServerLevel level, int chunkX, int chunkZ, boolean generateChunks) {
        try {
            if (generateChunks) {
                level.getChunk(chunkX, chunkZ);
                return true;
            }
            return WorldUtil.provideChunk(level, chunkX, chunkZ).isPresent();
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
