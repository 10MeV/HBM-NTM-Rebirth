package com.hbm.ntm.bullet;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class BulletConfigSyncRegistry {
    public static final int TEST_CONFIG = 0;
    public static final int TURBINE = 1;
    public static final int MASKMAN_BULLET = 2;
    public static final int MASKMAN_ORB = 3;
    public static final int MASKMAN_BOLT = 4;
    public static final int MASKMAN_ROCKET = 5;
    public static final int MASKMAN_TRACER = 6;
    public static final int MASKMAN_METEOR = 7;
    public static final int WORM_BOLT = 8;
    public static final int WORM_LASER = 9;
    public static final int UFO_ROCKET = 10;
    public static final int G12_BP = 41;
    public static final int G12_BP_MAGNUM = 42;
    public static final int G12_BP_SLUG = 43;
    public static final int G12 = 44;
    public static final int G12_SLUG = 45;
    public static final int G12_FLECHETTE = 46;
    public static final int G12_MAGNUM = 47;
    public static final int G12_EXPLOSIVE = 48;
    public static final int G12_PHOSPHORUS = 49;
    public static final int SEDNA_RUNTIME_COMPLEX_START = 50;

    private static final Map<Integer, BulletConfig> BY_LEGACY_ID = new LinkedHashMap<>();
    private static final Map<BulletConfig, Integer> LEGACY_ID_BY_CONFIG = new IdentityHashMap<>();
    private static boolean initialized;

    public static synchronized void bootstrap() {
        if (initialized) {
            return;
        }
        register(TURBINE, LegacyBulletConfigs.TURBINE);
        register(MASKMAN_BULLET, LegacyBulletConfigs.MASKMAN_BULLET);
        register(MASKMAN_ORB, LegacyBulletConfigs.MASKMAN_ORB);
        register(MASKMAN_BOLT, LegacyBulletConfigs.MASKMAN_BOLT);
        register(MASKMAN_ROCKET, LegacyBulletConfigs.MASKMAN_ROCKET);
        register(MASKMAN_TRACER, LegacyBulletConfigs.MASKMAN_TRACER);
        register(MASKMAN_METEOR, LegacyBulletConfigs.MASKMAN_METEOR);
        register(WORM_BOLT, LegacyBulletConfigs.WORM_BOLT);
        register(WORM_LASER, LegacyBulletConfigs.WORM_LASER);
        register(UFO_ROCKET, LegacyBulletConfigs.UFO_ROCKET);
        register(G12_BP, LegacySednaRuntimeBulletConfigs.G12_BP);
        register(G12_BP_MAGNUM, LegacySednaRuntimeBulletConfigs.G12_BP_MAGNUM);
        register(G12_BP_SLUG, LegacySednaRuntimeBulletConfigs.G12_BP_SLUG);
        register(G12, LegacySednaRuntimeBulletConfigs.G12);
        register(G12_SLUG, LegacySednaRuntimeBulletConfigs.G12_SLUG);
        register(G12_FLECHETTE, LegacySednaRuntimeBulletConfigs.G12_FLECHETTE);
        register(G12_MAGNUM, LegacySednaRuntimeBulletConfigs.G12_MAGNUM);
        register(G12_EXPLOSIVE, LegacySednaRuntimeBulletConfigs.G12_EXPLOSIVE);
        register(G12_PHOSPHORUS, LegacySednaRuntimeBulletConfigs.G12_PHOSPHORUS);
        registerRange(SEDNA_RUNTIME_COMPLEX_START, LegacySednaRuntimeBulletConfigs.allAdditionalSynced());
        initialized = true;
    }

    public static Optional<BulletConfig> pullConfig(int legacyId) {
        bootstrap();
        return Optional.ofNullable(BY_LEGACY_ID.get(legacyId));
    }

    public static BulletSyncedState syncedState(int legacyId) {
        return BulletSyncedState.fromLegacyId(legacyId);
    }

    public static int getLegacyId(BulletConfig config) {
        bootstrap();
        return LEGACY_ID_BY_CONFIG.getOrDefault(config, -1);
    }

    public static BulletSyncedState syncedState(BulletConfig config) {
        return BulletSyncedState.fromConfig(config);
    }

    public static Collection<BulletConfig> syncedConfigs() {
        bootstrap();
        return Collections.unmodifiableCollection(BY_LEGACY_ID.values());
    }

    private static void register(int legacyId, BulletConfig config) {
        if (BY_LEGACY_ID.putIfAbsent(legacyId, config) != null) {
            throw new IllegalStateException("Duplicate bullet config legacy id " + legacyId);
        }
        LEGACY_ID_BY_CONFIG.put(config, legacyId);
    }

    private static void registerRange(int firstLegacyId, Iterable<BulletConfig> configs) {
        int legacyId = firstLegacyId;
        for (BulletConfig config : configs) {
            register(legacyId++, config);
        }
    }

    private BulletConfigSyncRegistry() {
    }
}
