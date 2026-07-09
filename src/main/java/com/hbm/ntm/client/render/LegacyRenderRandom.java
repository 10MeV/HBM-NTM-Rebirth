package com.hbm.ntm.client.render;

import java.util.Random;

public final class LegacyRenderRandom {
    private static final ThreadLocal<Random> PRIMARY = ThreadLocal.withInitial(Random::new);
    private static final ThreadLocal<Random> SECONDARY = ThreadLocal.withInitial(Random::new);

    public static Random seeded(long seed) {
        Random random = PRIMARY.get();
        random.setSeed(seed);
        return random;
    }

    public static Random seededSecondary(long seed) {
        Random random = SECONDARY.get();
        random.setSeed(seed);
        return random;
    }

    private LegacyRenderRandom() {
    }
}
