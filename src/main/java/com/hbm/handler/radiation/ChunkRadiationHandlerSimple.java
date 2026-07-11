package com.hbm.handler.radiation;

import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

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
        com.hbm.ntm.radiation.ChunkRadiationManager.updateNow(ServerLifecycleHooks.getCurrentServer());
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

    @Override
    public void receiveWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            com.hbm.ntm.radiation.ChunkRadiationManager.loadLevel(level);
        }
    }

    @Override
    public void receiveWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            com.hbm.ntm.radiation.ChunkRadiationManager.unloadLevel(level);
        }
    }

    @Override
    public void receiveChunkLoad(ChunkDataEvent.Load event) {
        if (event.getChunk().getWorldForge() instanceof ServerLevel level) {
            com.hbm.ntm.radiation.ChunkRadiationManager.loadChunkData(level, event.getChunk().getPos(),
                    event.getData());
        }
    }

    @Override
    public void receiveChunkSave(ChunkDataEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel level) {
            com.hbm.ntm.radiation.ChunkRadiationManager.saveChunkData(level, event.getChunk().getPos(),
                    event.getData());
        }
    }

    @Override
    public void receiveChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            com.hbm.ntm.radiation.ChunkRadiationManager.unloadChunk(level, event.getChunk().getPos());
        }
    }

    @Override
    public void handleWorldDestruction() {
        com.hbm.ntm.radiation.ChunkRadiationManager.handleWorldEffectsNow(ServerLifecycleHooks.getCurrentServer());
    }

    public static class SimpleRadiationPerWorld {
        public Map<ChunkCoordIntPair, Float> radiation = new HashMap<>();
    }
}
