package com.hbm.handler.radiation;

import net.minecraft.world.level.Level;

/**
 * Source-backed no-op handler from 1.7.10, kept only for old call-shape
 * compatibility. It is not a modern runtime mode switch.
 */
@Deprecated(forRemoval = false)
public class ChunkRadiationHandlerBlank extends ChunkRadiationHandler {
    @Override
    public void updateSystem() {
    }

    @Override
    public float getRadiation(Level level, int x, int y, int z) {
        return 0.0F;
    }

    @Override
    public void setRadiation(Level level, int x, int y, int z, float rad) {
    }

    @Override
    public void incrementRad(Level level, int x, int y, int z, float rad) {
    }

    @Override
    public void decrementRad(Level level, int x, int y, int z, float rad) {
    }

    @Override
    public void clearSystem(Level level) {
    }
}
