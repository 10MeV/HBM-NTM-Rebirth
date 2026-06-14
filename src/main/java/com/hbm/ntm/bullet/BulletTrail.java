package com.hbm.ntm.bullet;

import java.util.Arrays;

public enum BulletTrail {
    LACUNAE(0),
    NIGHTMARE(1),
    LASER(2),
    ZOMG(3),
    WORM(4),
    GLASS_CYAN(5),
    GLASS_BLUE(6),
    SEDNA_LIGHTNING(100),
    SEDNA_LIGHTNING_SUB(101),
    SEDNA_CRACKLE(102),
    SEDNA_BLACK_LIGHTNING(103),
    SEDNA_NI4NI(104),
    SEDNA_FOLLY(105);

    private final int legacyId;

    BulletTrail(int legacyId) {
        this.legacyId = legacyId;
    }

    public int legacyId() {
        return legacyId;
    }

    public static BulletTrail fromLegacyId(int legacyId) {
        return Arrays.stream(values())
                .filter(trail -> trail.legacyId == legacyId)
                .findFirst()
                .orElse(LACUNAE);
    }
}
