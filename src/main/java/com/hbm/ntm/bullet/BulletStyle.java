package com.hbm.ntm.bullet;

import java.util.Arrays;

public enum BulletStyle {
    NONE(-1),
    NORMAL(0),
    PISTOL(1),
    FLECHETTE(2),
    PELLET(3),
    BOLT(4),
    FOLLY(5),
    ROCKET(6),
    STINGER(7),
    GRENADE(10),
    BALEFIRE(11),
    ORB(12),
    APDS(14),
    BLADE(15),
    TAU(17),
    LEADBURSTER(18);

    private final int legacyId;

    BulletStyle(int legacyId) {
        this.legacyId = legacyId;
    }

    public int legacyId() {
        return legacyId;
    }

    public static BulletStyle fromLegacyId(int legacyId) {
        return Arrays.stream(values())
                .filter(style -> style.legacyId == legacyId)
                .findFirst()
                .orElse(NONE);
    }
}
