package com.hbm.ntm.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class HbmLegacyWireRenderMath {
    public static final int PYLON_HANG_SEGMENTS = 10;
    public static final double PYLON_MAX_HANG = 2.5D;
    public static final double PYLON_HANG_DIVISOR = 15.0D;

    public static int pylonSecondMountIndex(int line, int secondMountCount, int lineCount,
            int firstLegacyMetadata, int secondLegacyMetadata) {
        int safeMountCount = Math.max(1, secondMountCount);
        int secondIndex = Math.floorMod(line, safeMountCount);
        if (lineCount == 4 && crossesLegacyFourWirePylons(firstLegacyMetadata, secondLegacyMetadata)) {
            secondIndex = Math.floorMod(secondIndex + 2, safeMountCount);
        }
        return secondIndex;
    }

    public static boolean crossesLegacyFourWirePylons(int firstLegacyMetadata, int secondLegacyMetadata) {
        int first = firstLegacyMetadata - 10;
        int second = secondLegacyMetadata - 10;
        return first == 5 && second == 2 || first == 2 && second == 5;
    }

    public static double pylonHang(double length) {
        return Math.min(length / PYLON_HANG_DIVISOR, PYLON_MAX_HANG);
    }

    public static int legacyMetadata(BlockState state) {
        Direction facing = getFacing(state);
        return 10 + facing.ordinal();
    }

    private static Direction getFacing(BlockState state) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
    }

    private HbmLegacyWireRenderMath() {
    }
}
