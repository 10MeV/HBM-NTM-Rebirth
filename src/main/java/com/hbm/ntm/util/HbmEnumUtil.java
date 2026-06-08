package com.hbm.ntm.util;

import net.minecraft.core.Direction;

public final class HbmEnumUtil {
    public static final Direction[] DIRECTIONS = Direction.values();
    public static final int[][] HORIZONTAL_OFFSETS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private HbmEnumUtil() {
    }

    public static <T extends Enum<T>> T grabEnumSafely(Class<T> enumType, int index) {
        T[] values = enumType.getEnumConstants();
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Enum type has no constants: " + enumType);
        }
        return values[Math.floorMod(index, values.length)];
    }
}
