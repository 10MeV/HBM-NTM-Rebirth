package com.hbm.ntm.world;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public enum LegacyRotation {
    NONE,
    CLOCKWISE_90,
    CLOCKWISE_180,
    COUNTERCLOCKWISE_90;

    public LegacyRotation add(LegacyRotation rotation) {
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

    public Rotation toVanilla() {
        return switch (this) {
            case NONE -> Rotation.NONE;
            case CLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
        };
    }

    public Direction rotate(Direction direction) {
        if (direction.getAxis().isVertical()) {
            return direction;
        }
        return switch (this) {
            case NONE -> direction;
            case CLOCKWISE_90 -> direction.getClockWise();
            case CLOCKWISE_180 -> direction.getOpposite();
            case COUNTERCLOCKWISE_90 -> direction.getCounterClockWise();
        };
    }

    public static LegacyRotation fromVanilla(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> CLOCKWISE_90;
            case CLOCKWISE_180 -> CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> COUNTERCLOCKWISE_90;
            case NONE -> NONE;
        };
    }

    public static LegacyRotation getBlockRotation(Direction direction) {
        return switch (direction) {
            case NORTH -> NONE;
            case SOUTH -> CLOCKWISE_180;
            case EAST -> COUNTERCLOCKWISE_90;
            case WEST -> CLOCKWISE_90;
            default -> NONE;
        };
    }
}
