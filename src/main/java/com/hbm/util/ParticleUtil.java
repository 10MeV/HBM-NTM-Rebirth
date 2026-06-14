package com.hbm.util;

import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for common particle helper calls.
 */
@Deprecated(forRemoval = false)
public final class ParticleUtil {
    public static final String TYPE_GAS_FLAME = com.hbm.ntm.particle.ParticleUtil.TYPE_GAS_FLAME;
    public static final String TYPE_DEBUG_DRONE = com.hbm.ntm.particle.ParticleUtil.TYPE_DEBUG_DRONE;

    private ParticleUtil() {
    }

    public static void spawnGasFlame(Level level, double x, double y, double z, double motionX, double motionY,
            double motionZ) {
        com.hbm.ntm.particle.ParticleUtil.spawnGasFlame(level, x, y, z, motionX, motionY, motionZ);
    }

    public static void spawnDroneLine(Level level, double x, double y, double z, double lineX, double lineY,
            double lineZ, int color) {
        com.hbm.ntm.particle.ParticleUtil.spawnDroneLine(level, x, y, z, lineX, lineY, lineZ, color);
    }
}
