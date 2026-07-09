package com.hbm.handler.radiation;

import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Legacy Simple handler facade. Calls are forwarded to the single modern
 * chunk-radiation runtime, which selects Simple or PRISM from modern config.
 */
@Deprecated(forRemoval = false)
public class ChunkRadiationHandlerSimple extends ChunkRadiationHandler {
    @Override
    public void updateSystem() {
        // Modern CommonForgeEvents ticks ChunkRadiationManager per ServerLevel.
    }

    @Override
    public float getRadiation(Level level, int x, int y, int z) {
        return com.hbm.ntm.radiation.ChunkRadiationManager.getRadiation(level, new BlockPos(x, y, z));
    }

    @Override
    public void setRadiation(Level level, int x, int y, int z, float rad) {
        com.hbm.ntm.radiation.ChunkRadiationManager.setRadiation(level, new BlockPos(x, y, z), rad);
    }

    @Override
    public void incrementRad(Level level, int x, int y, int z, float rad) {
        com.hbm.ntm.radiation.ChunkRadiationManager.incrementRadiation(level, new BlockPos(x, y, z), rad);
    }

    @Override
    public void decrementRad(Level level, int x, int y, int z, float rad) {
        com.hbm.ntm.radiation.ChunkRadiationManager.decrementRadiation(level, new BlockPos(x, y, z), rad);
    }

    @Override
    public void clearSystem(Level level) {
        com.hbm.ntm.radiation.ChunkRadiationManager.clear(level);
    }

    public static class SimpleRadiationPerWorld {
        public Map<ChunkCoordIntPair, Float> radiation = new HashMap<>();
    }
}
