package com.hbm.ntm.explosion;

import net.minecraft.world.level.Level;

/**
 * Compatibility entry for the legacy threaded MK5 ray explosion.
 *
 * <p>The 1.7.10 version reads chunk storage snapshots on worker threads and
 * mutates chunks directly during destruction. In 1.20.1 the safe implementation
 * path is the already ported batched main-thread worker, so this class preserves
 * the old API while delegating to that budgeted implementation.</p>
 */
public class ExplosionNukeRayParallelized implements ExplosionRay {
    private final ExplosionNukeRayBatched delegate;

    public ExplosionNukeRayParallelized(Level level, double x, double y, double z, int strength, int speed, int radius) {
        this.delegate = new ExplosionNukeRayBatched(level, (int) x, (int) y, (int) z,
                strength, speed, radius);
    }

    @Override
    public void cacheChunksTick(int processTimeMs) {
        delegate.cacheChunksTick(processTimeMs);
    }

    @Override
    public void destructionTick(int processTimeMs) {
        delegate.destructionTick(processTimeMs);
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isComplete() {
        return delegate.isComplete();
    }
}
