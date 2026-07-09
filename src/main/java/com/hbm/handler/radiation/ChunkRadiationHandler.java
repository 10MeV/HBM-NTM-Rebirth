package com.hbm.handler.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;

/**
 * Legacy chunk-radiation handler call shape. Modern runtime ownership stays in
 * {@link com.hbm.ntm.radiation.ChunkRadiationManager}.
 */
@Deprecated(forRemoval = false)
public abstract class ChunkRadiationHandler {
    public abstract void updateSystem();

    public abstract float getRadiation(Level level, int x, int y, int z);

    public abstract void setRadiation(Level level, int x, int y, int z, float rad);

    public abstract void incrementRad(Level level, int x, int y, int z, float rad);

    public abstract void decrementRad(Level level, int x, int y, int z, float rad);

    public abstract void clearSystem(Level level);

    public float getRadiation(Level level, BlockPos pos) {
        return pos == null ? 0.0F : getRadiation(level, pos.getX(), pos.getY(), pos.getZ());
    }

    public void setRadiation(Level level, BlockPos pos, float rad) {
        if (pos != null) {
            setRadiation(level, pos.getX(), pos.getY(), pos.getZ(), rad);
        }
    }

    public void incrementRad(Level level, BlockPos pos, float rad) {
        if (pos != null) {
            incrementRad(level, pos.getX(), pos.getY(), pos.getZ(), rad);
        }
    }

    public void decrementRad(Level level, BlockPos pos, float rad) {
        if (pos != null) {
            decrementRad(level, pos.getX(), pos.getY(), pos.getZ(), rad);
        }
    }

    public void receiveWorldLoad(LevelEvent.Load event) {
    }

    public void receiveWorldUnload(LevelEvent.Unload event) {
    }

    public void receiveWorldTick(TickEvent.ServerTickEvent event) {
    }

    public void receiveChunkLoad(ChunkDataEvent.Load event) {
    }

    public void receiveChunkSave(ChunkDataEvent.Save event) {
    }

    public void receiveChunkUnload(ChunkEvent.Unload event) {
    }

    public void handleWorldDestruction() {
    }
}
