package com.hbm.ntm.neutron;

import net.minecraft.core.Direction;

public final class PileGraphiteMetadata {
    public static final int ORIENTATION_MASK = 0b0011;
    public static final int ALUMINUM_MASK = 0b0100;
    public static final int ACTIVE_MASK = 0b1000;

    private PileGraphiteMetadata() {
    }

    public static int orientation(int meta) {
        return meta & ORIENTATION_MASK;
    }

    public static boolean hasAluminum(int meta) {
        return (meta & ALUMINUM_MASK) == ALUMINUM_MASK;
    }

    public static boolean isActive(int meta) {
        return (meta & ACTIVE_MASK) == ACTIVE_MASK;
    }

    public static int toggleActive(int meta) {
        return meta ^ ACTIVE_MASK;
    }

    public static int preserveAluminum(int newMeta, int oldMeta) {
        return (newMeta & ~ALUMINUM_MASK) | (oldMeta & ALUMINUM_MASK);
    }

    public static boolean sideMatchesAxis(int meta, Direction side) {
        return side != null && sideAxisIndex(side) == orientation(meta);
    }

    public static Direction.Axis axis(int meta) {
        return switch (orientation(meta)) {
            case 0 -> Direction.Axis.Y;
            case 1 -> Direction.Axis.Z;
            case 2 -> Direction.Axis.X;
            default -> Direction.Axis.Y;
        };
    }

    public static Direction positiveDirection(int meta) {
        return switch (axis(meta)) {
            case X -> Direction.EAST;
            case Y -> Direction.UP;
            case Z -> Direction.SOUTH;
        };
    }

    public static int orientationForSide(Direction side) {
        return sideAxisIndex(side);
    }

    private static int sideAxisIndex(Direction side) {
        return switch (side.getAxis()) {
            case Y -> 0;
            case Z -> 1;
            case X -> 2;
        };
    }
}
