package com.hbm.ntm.util.fauxpointtwelve;

import net.minecraft.core.Direction;

/**
 * Legacy 1.12-style horizontal rotation helper.
 */
@Deprecated(forRemoval = false)
public enum Rotation {
    NONE,
    CLOCKWISE_90,
    CLOCKWISE_180,
    COUNTERCLOCKWISE_90;

    public Rotation add(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> switch (this) {
                case NONE -> CLOCKWISE_180;
                case CLOCKWISE_90 -> COUNTERCLOCKWISE_90;
                case CLOCKWISE_180 -> NONE;
                case COUNTERCLOCKWISE_90 -> CLOCKWISE_90;
            };
            case COUNTERCLOCKWISE_90 -> switch (this) {
                case NONE -> COUNTERCLOCKWISE_90;
                case CLOCKWISE_90 -> NONE;
                case CLOCKWISE_180 -> CLOCKWISE_90;
                case COUNTERCLOCKWISE_90 -> CLOCKWISE_180;
            };
            case CLOCKWISE_90 -> switch (this) {
                case NONE -> CLOCKWISE_90;
                case CLOCKWISE_90 -> CLOCKWISE_180;
                case CLOCKWISE_180 -> COUNTERCLOCKWISE_90;
                case COUNTERCLOCKWISE_90 -> NONE;
            };
            case NONE -> this;
        };
    }

    public static Rotation getBlockRotation(Direction direction) {
        return switch (direction) {
            case NORTH -> NONE;
            case SOUTH -> CLOCKWISE_180;
            case EAST -> COUNTERCLOCKWISE_90;
            case WEST -> CLOCKWISE_90;
            default -> NONE;
        };
    }
}
