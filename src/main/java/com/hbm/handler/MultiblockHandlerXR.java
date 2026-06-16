package com.hbm.handler;

import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockExtents;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

/**
 * Modern bridge for the legacy 1.7.10 {@code MultiblockHandlerXR} helper.
 */
@Deprecated(forRemoval = false)
public final class MultiblockHandlerXR {
    private static final int MAX_LEGACY_POSITIONS = 2000;

    public static final int[] uni = new int[] { 3, 0, 4, 4, 4, 4 };

    public static boolean checkSpace(Level level, int x, int y, int z, int[] dimensions,
            int temporaryX, int temporaryY, int temporaryZ, Direction facing) {
        return checkSpace(level, new BlockPos(x, y, z), dimensions,
                new BlockPos(temporaryX, temporaryY, temporaryZ), facing);
    }

    public static boolean checkSpace(Level level, BlockPos corePos, int[] dimensions,
            BlockPos temporaryPos, Direction facing) {
        if (!isSafeLegacySize(dimensions, facing)) {
            return false;
        }
        return MultiblockHelper.checkSpace(level, corePos,
                LegacyMultiblockLayout.legacyXrCheckOffsets(dimensions, horizontalOrSouth(facing)), temporaryPos);
    }

    public static void fillSpace(Level level, int x, int y, int z, int[] dimensions, Direction facing) {
        fillSpace(level, new BlockPos(x, y, z), dimensions, facing);
    }

    public static void fillSpace(Level level, BlockPos corePos, int[] dimensions, Direction facing) {
        fillSpace(level, corePos, dimensions, ModBlocks.DUMMY_BLOCK.get().defaultBlockState(), facing);
    }

    public static void fillSpace(Level level, BlockPos corePos, int[] dimensions, BlockState dummyState,
            Direction facing) {
        if (!isSafeLegacySize(dimensions, facing)) {
            return;
        }
        MultiblockHelper.fillOffsets(level, corePos,
                LegacyMultiblockLayout.legacyXrFillOffsets(dimensions, horizontalOrSouth(facing)), dummyState);
    }

    public static void fillSpaceWithProxyModes(Level level, BlockPos corePos, int[] dimensions, BlockState dummyState,
            Direction facing, Predicate<BlockPos> proxyOffsets, LegacyProxyMode mode) {
        if (!isSafeLegacySize(dimensions, facing)) {
            return;
        }
        LegacyProxyMode proxyMode = mode == null ? LegacyProxyMode.fullCombo() : mode;
        MultiblockHelper.fillOffsetsWithProxyModes(level, corePos,
                LegacyMultiblockLayout.legacyXrFillOffsets(dimensions, horizontalOrSouth(facing)), dummyState,
                offset -> proxyOffsets.test(offset) ? proxyMode : LegacyProxyMode.none());
    }

    @Deprecated(forRemoval = false)
    public static void emptySpace(Level level, int x, int y, int z, int[] dimensions, Direction facing) {
        emptySpace(level, new BlockPos(x, y, z), dimensions, facing);
    }

    @Deprecated(forRemoval = false)
    public static void emptySpace(Level level, BlockPos corePos, int[] dimensions, Direction facing) {
        if (!isSafeLegacySize(dimensions, facing)) {
            return;
        }
        MultiblockHelper.removeOffsets(level, corePos,
                LegacyMultiblockLayout.legacyXrFillOffsets(dimensions, horizontalOrSouth(facing)));
    }

    public static int[] rotate(int[] dimensions, Direction facing) {
        return dimensions == null ? null : MultiblockExtents.rotateLegacyXr(dimensions, horizontalOrSouth(facing));
    }

    public static List<BlockPos> fillOffsets(int[] dimensions, Direction facing) {
        return LegacyMultiblockLayout.legacyXrFillOffsets(dimensions, horizontalOrSouth(facing));
    }

    public static List<BlockPos> checkOffsets(int[] dimensions, Direction facing) {
        return LegacyMultiblockLayout.legacyXrCheckOffsets(dimensions, horizontalOrSouth(facing));
    }

    public static boolean isSafeLegacySize(int[] dimensions, Direction facing) {
        int[] rotated = rotate(dimensions, facing);
        if (rotated == null || rotated.length != 6) {
            return false;
        }
        long xRange = rangeSize(-rotated[4], rotated[5]);
        long yRange = rangeSize(-rotated[1], rotated[0]);
        long zRange = rangeSize(-rotated[2], rotated[3]);
        long positions = xRange * yRange * zRange;
        return positions <= MAX_LEGACY_POSITIONS + 1L;
    }

    private static long rangeSize(int min, int max) {
        return Math.max(0L, (long) max - min + 1L);
    }

    private static Direction horizontalOrSouth(Direction facing) {
        return facing == null || facing.getAxis().isVertical() ? Direction.SOUTH : facing;
    }

    private MultiblockHandlerXR() {
    }
}
