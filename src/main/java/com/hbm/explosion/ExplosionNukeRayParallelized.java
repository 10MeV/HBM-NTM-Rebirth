package com.hbm.explosion;

import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;

/**
 * Legacy 1.7.10 package bridge for the inactive threaded MK5 ray entrypoint.
 */
@Deprecated(forRemoval = false)
public class ExplosionNukeRayParallelized extends com.hbm.ntm.explosion.ExplosionNukeRayParallelized
        implements com.hbm.interfaces.IExplosionRay {
    public ExplosionNukeRayParallelized(Level level, double x, double y, double z, int strength, int speed, int radius) {
        super(level, x, y, z, strength, speed, radius);
    }

    public ExplosionNukeRayParallelized(Level level, double x, double y, double z, int strength, int speed, int radius,
            BiConsumer<Integer, Integer> chunkLoader) {
        super(level, x, y, z, strength, speed, radius, chunkLoader);
    }
}
