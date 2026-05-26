package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public final class LegacyMultiblockOffsets {
    private LegacyMultiblockOffsets() {
    }

    public static List<BlockPos> xrBox(int[] dimensions, Direction facing, BlockPos originOffset) {
        return xrBox(dimensions, facing, originOffset, false);
    }

    public static List<BlockPos> xrBox(int[] dimensions, Direction facing, BlockPos originOffset,
            boolean includeBoxOrigin) {
        int[] rotated = MultiblockExtents.rotateLegacyXr(dimensions, facing);
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = originOffset.getX() - rotated[4]; x <= originOffset.getX() + rotated[5]; x++) {
            for (int y = originOffset.getY() - rotated[1]; y <= originOffset.getY() + rotated[0]; y++) {
                for (int z = originOffset.getZ() - rotated[2]; z <= originOffset.getZ() + rotated[3]; z++) {
                    BlockPos offset = new BlockPos(x, y, z);
                    if (!includeBoxOrigin && offset.equals(originOffset)) {
                        continue;
                    }
                    if (!offset.equals(BlockPos.ZERO)) {
                        offsets.add(offset);
                    }
                }
            }
        }
        return List.copyOf(offsets);
    }

    public static BlockPos relative(Direction facing, int forward, int side) {
        return relative(facing, legacyUpSide(facing), forward, side, 0);
    }

    public static BlockPos relative(Direction facing, int forward, int side, int y) {
        return relative(facing, legacyUpSide(facing), forward, side, y);
    }

    public static BlockPos relative(Direction facing, Direction sideAxis, int forward, int side, int y) {
        return new BlockPos(
                facing.getStepX() * forward + sideAxis.getStepX() * side,
                y,
                facing.getStepZ() * forward + sideAxis.getStepZ() * side);
    }

    public static Direction legacyUpSide(Direction facing) {
        return facing.getClockWise();
    }

    public static Direction legacyDownSide(Direction facing) {
        return facing.getCounterClockWise();
    }

    public static List<BlockPos> lineAlongFacing(Direction facing, Direction sideAxis, int minForward, int maxForward,
            int side, int y) {
        List<BlockPos> offsets = new ArrayList<>();
        for (int forward = minForward; forward <= maxForward; forward++) {
            offsets.add(relative(facing, sideAxis, forward, side, y));
        }
        return List.copyOf(offsets);
    }

    public static List<BlockPos> lineAlongSide(Direction facing, Direction sideAxis, int forward, int minSide,
            int maxSide, int y) {
        List<BlockPos> offsets = new ArrayList<>();
        for (int side = minSide; side <= maxSide; side++) {
            offsets.add(relative(facing, sideAxis, forward, side, y));
        }
        return List.copyOf(offsets);
    }

    @SafeVarargs
    public static List<BlockPos> combine(Iterable<BlockPos>... offsetGroups) {
        List<BlockPos> offsets = new ArrayList<>();
        for (Iterable<BlockPos> group : offsetGroups) {
            for (BlockPos offset : group) {
                offsets.add(offset);
            }
        }
        return List.copyOf(offsets);
    }

    public static List<BlockPos> floorCorners(int radius) {
        return List.of(
                new BlockPos(radius, 0, radius),
                new BlockPos(radius, 0, -radius),
                new BlockPos(-radius, 0, radius),
                new BlockPos(-radius, 0, -radius));
    }

    public static List<BlockPos> cardinal(int radius) {
        return cardinal(radius, 0);
    }

    public static List<BlockPos> cardinal(int radius, int y) {
        return List.of(
                new BlockPos(radius, y, 0),
                new BlockPos(-radius, y, 0),
                new BlockPos(0, y, radius),
                new BlockPos(0, y, -radius));
    }

    public static List<BlockPos> squarePerimeter(int radius) {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) == radius || Math.abs(z) == radius) {
                    offsets.add(new BlockPos(x, 0, z));
                }
            }
        }
        return List.copyOf(offsets);
    }

    public static List<BlockPos> squareSidesWithoutCorners(int radius) {
        List<BlockPos> offsets = new ArrayList<>();
        for (int z = -radius + 1; z <= radius - 1; z++) {
            offsets.add(new BlockPos(radius, 0, z));
        }
        for (int z = -radius + 1; z <= radius - 1; z++) {
            offsets.add(new BlockPos(-radius, 0, z));
        }
        for (int x = -radius + 1; x <= radius - 1; x++) {
            offsets.add(new BlockPos(x, 0, radius));
        }
        for (int x = -radius + 1; x <= radius - 1; x++) {
            offsets.add(new BlockPos(x, 0, -radius));
        }
        return List.copyOf(offsets);
    }

    public static boolean isSquarePerimeter(BlockPos offset, int radius, int y) {
        return offset.getY() == y
                && (Math.abs(offset.getX()) == radius || Math.abs(offset.getZ()) == radius)
                && Math.abs(offset.getX()) <= radius
                && Math.abs(offset.getZ()) <= radius;
    }
}
