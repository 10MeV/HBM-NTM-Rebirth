package com.hbm.ntm.explosion;

import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;

/**
 * Compatibility entry for the legacy threaded MK5 ray explosion.
 *
 * <p>The 1.7.10 version reads chunk storage snapshots on worker threads and
 * mutates chunks directly during destruction. In 1.20.1 the safe implementation
 * path is the already ported batched main-thread worker, so this class preserves
 * the old API while delegating to that budgeted implementation.</p>
 */
public class ExplosionNukeRayParallelized extends ExplosionNukeRayBatched {
    public ExplosionNukeRayParallelized(Level level, double x, double y, double z, int strength, int speed, int radius) {
        super(level, (int) x, (int) y, (int) z, strength, speed, radius);
    }

    public ExplosionNukeRayParallelized(Level level, double x, double y, double z, int strength, int speed, int radius,
            BiConsumer<Integer, Integer> chunkLoader) {
        super(level, (int) x, (int) y, (int) z, strength, speed, radius, chunkLoader);
    }
}
