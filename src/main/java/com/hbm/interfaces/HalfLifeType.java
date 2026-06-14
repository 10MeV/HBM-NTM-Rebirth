package com.hbm.interfaces;

/**
 * Legacy 1.7.10 package bridge for half-life display buckets.
 */
@Deprecated(forRemoval = false)
public enum HalfLifeType {
    SHORT,
    MEDIUM,
    LONG;

    public com.hbm.ntm.api.common.HalfLifeType toModern() {
        return com.hbm.ntm.api.common.HalfLifeType.valueOf(name());
    }

    public static HalfLifeType fromModern(com.hbm.ntm.api.common.HalfLifeType type) {
        return valueOf(type.name());
    }
}
