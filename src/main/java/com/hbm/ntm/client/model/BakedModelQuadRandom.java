package com.hbm.ntm.client.model;

import net.minecraft.util.RandomSource;

final class BakedModelQuadRandom {
    static final long BAKE_SEED = 42L;

    private static final ThreadLocal<RandomSource> RANDOM =
            ThreadLocal.withInitial(() -> RandomSource.create(BAKE_SEED));

    static RandomSource seeded() {
        RandomSource random = RANDOM.get();
        random.setSeed(BAKE_SEED);
        return random;
    }

    private BakedModelQuadRandom() {
    }
}
