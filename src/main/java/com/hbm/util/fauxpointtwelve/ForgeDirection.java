package com.hbm.util.fauxpointtwelve;

import net.minecraft.core.Direction;

/**
 * Legacy Forge 1.7.10 direction data carrier.
 */
@Deprecated(forRemoval = false)
public enum ForgeDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0),
    UNKNOWN(0, 0, 0);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;

    public static final ForgeDirection[] VALID_DIRECTIONS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};
    public static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4, 6};
    public static final int[][] ROTATION_MATRIX = {
            {0, 1, 4, 5, 3, 2, 6},
            {0, 1, 5, 4, 2, 3, 6},
            {5, 4, 2, 3, 0, 1, 6},
            {4, 5, 2, 3, 1, 0, 6},
            {2, 3, 1, 0, 4, 5, 6},
            {3, 2, 0, 1, 4, 5, 6},
            {0, 1, 2, 3, 4, 5, 6},
    };

    ForgeDirection(int x, int y, int z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        flag = 1 << ordinal();
    }

    public static ForgeDirection getOrientation(int id) {
        if (id >= 0 && id < VALID_DIRECTIONS.length) {
            return VALID_DIRECTIONS[id];
        }
        return UNKNOWN;
    }

    public ForgeDirection getOpposite() {
        return getOrientation(OPPOSITES[ordinal()]);
    }

    public ForgeDirection getRotation(ForgeDirection axis) {
        return getOrientation(ROTATION_MATRIX[axis.ordinal()][ordinal()]);
    }

    public Direction toDirection() {
        return switch (this) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
            case UNKNOWN -> null;
        };
    }

    public static ForgeDirection fromDirection(Direction direction) {
        if (direction == null) {
            return UNKNOWN;
        }
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }
}
