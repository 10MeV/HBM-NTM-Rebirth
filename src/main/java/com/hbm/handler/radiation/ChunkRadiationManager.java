package com.hbm.handler.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;

/**
 * Legacy package facade for the 1.7.10 chunk radiation manager.
 */
@Deprecated(forRemoval = false)
public class ChunkRadiationManager {
    public static ChunkRadiationHandler proxy = new ChunkRadiationHandlerSimple();
    private int eggTimer = 0;

    public ChunkRadiationManager() {
    }

    public static float getRadiation(Level level, int x, int y, int z) {
        return proxy.getRadiation(level, x, y, z);
    }

    public static float getRadiation(Level level, BlockPos pos) {
        return proxy.getRadiation(level, pos);
    }

    public static void setRadiation(Level level, int x, int y, int z, float rad) {
        proxy.setRadiation(level, x, y, z, rad);
    }

    public static void setRadiation(Level level, BlockPos pos, float rad) {
        proxy.setRadiation(level, pos, rad);
    }

    public static void incrementRad(Level level, int x, int y, int z, float rad) {
        proxy.incrementRad(level, x, y, z, rad);
    }

    public static void incrementRad(Level level, BlockPos pos, float rad) {
        proxy.incrementRad(level, pos, rad);
    }

    public static void decrementRad(Level level, int x, int y, int z, float rad) {
        proxy.decrementRad(level, x, y, z, rad);
    }

    public static void decrementRad(Level level, BlockPos pos, float rad) {
        proxy.decrementRad(level, pos, rad);
    }

    public static void clearSystem(Level level) {
        proxy.clearSystem(level);
    }

    public void onWorldLoad(LevelEvent.Load event) {
        if (!com.hbm.config.RadiationConfig.enableChunkRads()) {
            return;
        }
        if (usesModernRuntime()) {
            return;
        }
        proxy.receiveWorldLoad(event);
    }

    public void onWorldUnload(LevelEvent.Unload event) {
        if (!com.hbm.config.RadiationConfig.enableChunkRads()) {
            return;
        }
        if (usesModernRuntime()) {
            if (event.getLevel() instanceof Level level && !level.isClientSide()) {
                com.hbm.ntm.radiation.ChunkRadiationManager.unloadLevel(level);
            }
            return;
        }
        proxy.receiveWorldUnload(event);
    }

    public void onChunkLoad(ChunkDataEvent.Load event) {
        if (!com.hbm.config.RadiationConfig.enableChunkRads()) {
            return;
        }
        if (usesModernRuntime()) {
            if (event.getChunk().getWorldForge() instanceof ServerLevel level) {
                com.hbm.ntm.radiation.ChunkRadiationManager.loadChunkData(level, event.getChunk().getPos(), event.getData());
            }
            return;
        }
        proxy.receiveChunkLoad(event);
    }

    public void onChunkSave(ChunkDataEvent.Save event) {
        if (!com.hbm.config.RadiationConfig.enableChunkRads()) {
            return;
        }
        if (usesModernRuntime()) {
            if (event.getLevel() instanceof ServerLevel level) {
                com.hbm.ntm.radiation.ChunkRadiationManager.saveChunkData(level, event.getChunk().getPos(), event.getData());
            }
            return;
        }
        proxy.receiveChunkSave(event);
    }

    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!com.hbm.config.RadiationConfig.enableChunkRads()) {
            return;
        }
        if (usesModernRuntime()) {
            if (event.getLevel() instanceof Level level && !level.isClientSide()) {
                com.hbm.ntm.radiation.ChunkRadiationManager.unloadChunk(level, event.getChunk().getPos());
            }
            return;
        }
        proxy.receiveChunkUnload(event);
    }

    public void updateSystem(TickEvent.ServerTickEvent event) {
        if (!com.hbm.config.RadiationConfig.enableChunkRads()
                || event.phase != TickEvent.Phase.END
                || event.getServer() == null) {
            return;
        }
        if (usesModernRuntime()) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                com.hbm.ntm.radiation.ChunkRadiationManager.tick(level);
            }
            return;
        }
        eggTimer++;
        if (eggTimer >= 20) {
            proxy.updateSystem();
            eggTimer = 0;
        }
        if (com.hbm.config.RadiationConfig.worldRadEffects()) {
            proxy.handleWorldDestruction();
        }
        proxy.receiveWorldTick(event);
    }

    private static boolean usesModernRuntime() {
        return proxy != null && proxy.getClass() == ChunkRadiationHandlerSimple.class;
    }
}
