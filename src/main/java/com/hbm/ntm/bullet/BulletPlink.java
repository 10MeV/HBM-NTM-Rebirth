package com.hbm.ntm.bullet;

import java.util.Arrays;

public enum BulletPlink {
    NONE(0),
    BULLET(1),
    GRENADE(2),
    ENERGY(3),
    SING(4);

    private final int legacyId;

    BulletPlink(int legacyId) {
        this.legacyId = legacyId;
    }

    public int legacyId() {
        return legacyId;
    }

    public static BulletPlink fromLegacyId(int legacyId) {
        return Arrays.stream(values())
                .filter(plink -> plink.legacyId == legacyId)
                .findFirst()
                .orElse(NONE);
    }
}
