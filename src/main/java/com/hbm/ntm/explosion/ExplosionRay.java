package com.hbm.ntm.explosion;

/**
 * Procedural explosion worker split into cache and destruction phases.
 */
public interface ExplosionRay {
    void cacheChunksTick(int processTimeMs);

    void destructionTick(int processTimeMs);

    void cancel();

    boolean isComplete();
}
