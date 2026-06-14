package com.hbm.util.fauxpointtwelve;

import net.minecraft.core.Direction;

/**
 * Legacy 1.7.10 package bridge for the faux-1.12 horizontal rotation enum.
 */
@Deprecated(forRemoval = false)
public enum Rotation {
    NONE,
    CLOCKWISE_90,
    CLOCKWISE_180,
    COUNTERCLOCKWISE_90;

    public Rotation add(Rotation rotation) {
        return fromModern(toModern().add(rotation == null
                ? com.hbm.ntm.util.fauxpointtwelve.Rotation.NONE
                : rotation.toModern()));
    }

    public com.hbm.ntm.util.fauxpointtwelve.Rotation toModern() {
        return switch (this) {
            case NONE -> com.hbm.ntm.util.fauxpointtwelve.Rotation.NONE;
            case CLOCKWISE_90 -> com.hbm.ntm.util.fauxpointtwelve.Rotation.CLOCKWISE_90;
            case CLOCKWISE_180 -> com.hbm.ntm.util.fauxpointtwelve.Rotation.CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> com.hbm.ntm.util.fauxpointtwelve.Rotation.COUNTERCLOCKWISE_90;
        };
    }

    public static Rotation fromModern(com.hbm.ntm.util.fauxpointtwelve.Rotation rotation) {
        if (rotation == null) {
            return NONE;
        }
        return switch (rotation) {
            case NONE -> NONE;
            case CLOCKWISE_90 -> CLOCKWISE_90;
            case CLOCKWISE_180 -> CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> COUNTERCLOCKWISE_90;
        };
    }

    public static Rotation getBlockRotation(Direction direction) {
        return fromModern(com.hbm.ntm.util.fauxpointtwelve.Rotation.getBlockRotation(direction));
    }
}
