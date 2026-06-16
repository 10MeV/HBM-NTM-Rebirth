package com.hbm.handler;

import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockExtents;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Modern bridge for the legacy 1.7.10 {@code MultiblockHandler} helper.
 */
@Deprecated(forRemoval = false)
public final class MultiblockHandler {
    public enum EnumDirection {
        North,
        East,
        South,
        West
    }

    public static final int[] iGenDimensionNorth = new int[] { 1, 1, 2, 0, 3, 2 };
    public static final int[] iGenDimensionEast = new int[] { 2, 3, 2, 0, 1, 1 };
    public static final int[] iGenDimensionSouth = new int[] { 1, 1, 2, 0, 2, 3 };
    public static final int[] iGenDimensionWest = new int[] { 3, 2, 2, 0, 1, 1 };
    public static final int[] centDimension = new int[] { 0, 0, 2, 0, 0, 0 };
    public static final int[] cyclDimension = new int[] { 1, 1, 5, 0, 1, 1 };
    public static final int[] wellDimension = new int[] { 1, 1, 5, 0, 1, 1 };
    public static final int[] flareDimension = new int[] { 1, 1, 9, 0, 1, 1 };
    public static final int[] drillDimension = new int[] { 1, 1, 3, 0, 1, 1 };
    public static final int[] assemblerDimensionNorth = new int[] { 2, 1, 1, 0, 1, 2 };
    public static final int[] assemblerDimensionEast = new int[] { 2, 1, 1, 0, 2, 1 };
    public static final int[] assemblerDimensionSouth = new int[] { 1, 2, 1, 0, 2, 1 };
    public static final int[] assemblerDimensionWest = new int[] { 1, 2, 1, 0, 1, 2 };
    public static final int[] chemplantDimensionNorth = new int[] { 2, 1, 2, 0, 1, 2 };
    public static final int[] chemplantDimensionEast = new int[] { 2, 1, 2, 0, 2, 1 };
    public static final int[] chemplantDimensionSouth = new int[] { 1, 2, 2, 0, 2, 1 };
    public static final int[] chemplantDimensionWest = new int[] { 1, 2, 2, 0, 1, 2 };
    public static final int[] fluidTankDimensionNS = new int[] { 1, 1, 2, 0, 2, 2 };
    public static final int[] fluidTankDimensionEW = new int[] { 2, 2, 2, 0, 1, 1 };
    public static final int[] refineryDimensions = new int[] { 1, 1, 8, 0, 1, 1 };
    public static final int[] pumpjackDimensionNorth = new int[] { 1, 1, 4, 0, 6, 0 };
    public static final int[] pumpjackDimensionEast = new int[] { 0, 6, 4, 0, 1, 1 };
    public static final int[] pumpjackDimensionSouth = new int[] { 1, 1, 4, 0, 0, 6 };
    public static final int[] pumpjackDimensionWest = new int[] { 6, 0, 4, 0, 1, 1 };
    public static final int[] turbofanDimensionNorth = new int[] { 1, 1, 2, 0, 3, 3 };
    public static final int[] turbofanDimensionEast = new int[] { 3, 3, 2, 0, 1, 1 };
    public static final int[] turbofanDimensionSouth = new int[] { 1, 1, 2, 0, 3, 3 };
    public static final int[] turbofanDimensionWest = new int[] { 3, 3, 2, 0, 1, 1 };
    public static final int[] AMSLimiterDimensionNorth = new int[] { 0, 0, 5, 0, 2, 2 };
    public static final int[] AMSLimiterDimensionEast = new int[] { 2, 2, 5, 0, 0, 0 };
    public static final int[] AMSLimiterDimensionSouth = new int[] { 0, 0, 5, 0, 2, 2 };
    public static final int[] AMSLimiterDimensionWest = new int[] { 2, 2, 5, 0, 0, 0 };
    public static final int[] AMSEmitterDimension = new int[] { 2, 2, 5, 0, 2, 2 };
    public static final int[] AMSBaseDimension = new int[] { 1, 1, 1, 0, 1, 1 };
    public static final int[] radGenDimensionNorth = new int[] { 4, 1, 2, 0, 1, 1 };
    public static final int[] radGenDimensionEast = new int[] { 1, 1, 2, 0, 4, 1 };
    public static final int[] radGenDimensionSouth = new int[] { 1, 4, 2, 0, 1, 1 };
    public static final int[] radGenDimensionWest = new int[] { 1, 1, 2, 0, 1, 4 };
    public static final int[] reactorSmallDimension = new int[] { 0, 0, 2, 0, 0, 0 };
    public static final int[] uf6Dimension = new int[] { 0, 0, 1, 0, 0, 0 };

    public static int EnumToInt(EnumDirection dir) {
        if (dir == EnumDirection.North) {
            return 2;
        }
        if (dir == EnumDirection.East) {
            return 5;
        }
        if (dir == EnumDirection.South) {
            return 3;
        }
        if (dir == EnumDirection.West) {
            return 4;
        }
        return 0;
    }

    public static EnumDirection IntToEnum(int dir) {
        if (dir == 2) {
            return EnumDirection.North;
        }
        if (dir == 5) {
            return EnumDirection.East;
        }
        if (dir == 3) {
            return EnumDirection.South;
        }
        if (dir == 4) {
            return EnumDirection.West;
        }
        return EnumDirection.North;
    }

    public static Direction toDirection(EnumDirection dir) {
        return switch (dir == null ? EnumDirection.North : dir) {
            case North -> Direction.NORTH;
            case East -> Direction.EAST;
            case South -> Direction.SOUTH;
            case West -> Direction.WEST;
        };
    }

    public static EnumDirection fromDirection(Direction direction) {
        return switch (direction == null ? Direction.NORTH : direction) {
            case EAST -> EnumDirection.East;
            case SOUTH -> EnumDirection.South;
            case WEST -> EnumDirection.West;
            default -> EnumDirection.North;
        };
    }

    public static boolean checkSpace(Level level, int x, int y, int z, int[] dimensions) {
        return checkSpace(level, new BlockPos(x, y, z), dimensions);
    }

    public static boolean checkSpace(Level level, BlockPos corePos, int[] dimensions) {
        return MultiblockHelper.checkSpace(level, corePos, MultiblockExtents.ofLegacy(dimensions));
    }

    public static boolean fillUp(Level level, int x, int y, int z, int[] dimensions) {
        return fillUp(level, new BlockPos(x, y, z), dimensions);
    }

    public static boolean fillUp(Level level, BlockPos corePos, int[] dimensions) {
        return fillUp(level, corePos, dimensions, ModBlocks.DUMMY_BLOCK.get().defaultBlockState());
    }

    public static boolean fillUp(Level level, BlockPos corePos, int[] dimensions, BlockState dummyState) {
        return MultiblockHelper.fillUp(level, corePos, MultiblockExtents.ofLegacy(dimensions), dummyState);
    }

    public static boolean fillUpWithProxyModes(Level level, BlockPos corePos, int[] dimensions,
            BlockState dummyState, Predicate<BlockPos> proxyOffsets) {
        return MultiblockHelper.fillUpWithProxyModes(level, corePos, MultiblockExtents.ofLegacy(dimensions),
                dummyState, offset -> proxyOffsets.test(offset) ? LegacyProxyMode.fullCombo() : LegacyProxyMode.none());
    }

    public static boolean removeAll(Level level, int x, int y, int z, int[] dimensions) {
        return removeAll(level, new BlockPos(x, y, z), dimensions);
    }

    public static boolean removeAll(Level level, BlockPos corePos, int[] dimensions) {
        return MultiblockHelper.removeAll(level, corePos, MultiblockExtents.ofLegacy(dimensions));
    }

    private MultiblockHandler() {
    }
}
