package com.hbm.ntm.api.explosion;

public interface ExplosionRay {
    void cacheChunksTick(int processTimeMs);

    void destructionTick(int processTimeMs);

    void cancel();

    boolean isComplete();
}
