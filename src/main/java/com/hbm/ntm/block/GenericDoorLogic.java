package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Server-side coordinate transcription for {@code TileEntityDoorGeneric}'s
 * temporary "extra" cells.  This is intentionally separate from the block
 * implementation: the generic-door blocks are not registered yet, while a
 * later block can use this exact set to add/remove its dummy cells.
 */
public final class GenericDoorLogic {
    private GenericDoorLogic() {
    }

    /**
     * Returns the legacy cells which are extras at {@code openTicks}.  The
     * core is excluded because 1.7.10 toggled {@code shouldUseBB} instead of
     * replacing the tile entity's own block.
     */
    public static Set<BlockPos> openCells(LegacyDoorDefinition definition, BlockPos corePos,
                                          Direction facing, int openTicks) {
        if (!facing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("generic doors require a horizontal facing");
        }

        Set<BlockPos> result = new LinkedHashSet<>();
        for (int rangeIndex = 0; rangeIndex < definition.doorOpenRanges().size(); rangeIndex++) {
            LegacyDoorDefinition.DoorOpenRange range = definition.doorOpenRanges().get(rangeIndex);
            float time = definition.doorRangeOpenTime(openTicks, rangeIndex);
            int length = Math.abs(range.tangentAmount1());
            int sign = Integer.signum(range.tangentAmount1());

            for (int j = 0; j < length; j++) {
                // Deliberately keeps the legacy (length - 1) denominator: a
                // one-cell range therefore has 0 / 0 and is never rejected.
                if ((float) j / Math.abs(length - 1) > time) {
                    break;
                }
                for (int k = 0; k < range.tangentAmount2(); k++) {
                    BlockPos local = rangeCell(range, j, k, sign);
                    BlockPos worldPos = corePos.offset(rotateForLegacyDoor(facing, local));
                    if (!worldPos.equals(corePos)) {
                        result.add(worldPos.immutable());
                    }
                }
            }
        }
        return Set.copyOf(result);
    }

    /**
     * Whether the old range traversal reaches the core itself.  The caller
     * uses this to mirror the {@code shouldUseBB = false} branch without
     * attempting to replace its own block with a dummy.
     */
    public static boolean rangeContainsCore(LegacyDoorDefinition definition, BlockPos corePos,
                                            Direction facing, int openTicks) {
        if (!facing.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("generic doors require a horizontal facing");
        }
        for (int rangeIndex = 0; rangeIndex < definition.doorOpenRanges().size(); rangeIndex++) {
            LegacyDoorDefinition.DoorOpenRange range = definition.doorOpenRanges().get(rangeIndex);
            float time = definition.doorRangeOpenTime(openTicks, rangeIndex);
            int length = Math.abs(range.tangentAmount1());
            int sign = Integer.signum(range.tangentAmount1());
            for (int j = 0; j < length; j++) {
                if ((float) j / Math.abs(length - 1) > time) {
                    break;
                }
                for (int k = 0; k < range.tangentAmount2(); k++) {
                    BlockPos local = rangeCell(range, j, k, sign);
                    if (corePos.offset(rotateForLegacyDoor(facing, local)).equals(corePos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Exact per-cell AABB conversion from
     * {@code BlockDoorGeneric#getBoundingBox}. The returned box is local to
     * {@code cellPos}; a caller should turn it into a VoxelShape directly.
     */
    public static AABB cellBounds(LegacyDoorDefinition definition, BlockPos corePos, Direction facing,
                                  BlockPos cellPos, boolean open, boolean forCollision) {
        BlockPos delta = cellPos.subtract(corePos);
        BlockPos local = localCellCoordinates(facing, delta);
        AABB box = definition.blockBounds(local.getX(), local.getY(), local.getZ(), open, forCollision);
        return switch (facing) {
            case NORTH -> normalized(1.0D - box.minX, box.minY, 1.0D - box.minZ,
                    1.0D - box.maxX, box.maxY, 1.0D - box.maxZ);
            case SOUTH -> box;
            case WEST -> normalized(1.0D - box.minZ, box.minY, box.minX,
                    1.0D - box.maxZ, box.maxY, box.maxX);
            case EAST -> normalized(box.minZ, box.minY, 1.0D - box.maxX,
                    box.maxZ, box.maxY, 1.0D - box.minX);
            default -> throw new IllegalArgumentException("generic doors require a horizontal facing");
        };
    }

    private static BlockPos rangeCell(LegacyDoorDefinition.DoorOpenRange range, int j, int k, int sign) {
        BlockPos start = new BlockPos(range.startX(), range.startY(), range.startZ());
        return switch (range.axis()) {
            case 0 -> start.offset(0, k, sign * j);
            case 1 -> start.offset(k, sign * j, 0);
            case 2 -> start.offset(sign * j, k, 0);
            default -> throw new IllegalStateException("validated legacy range axis became invalid");
        };
    }

    /** Exact inverse-side local coordinate transform used by the old AABB lookup. */
    private static BlockPos localCellCoordinates(Direction facing, BlockPos delta) {
        return switch (facing) {
            case NORTH, WEST -> new BlockPos(delta.getZ(), delta.getY(), -delta.getX());
            case SOUTH, EAST -> new BlockPos(-delta.getZ(), delta.getY(), delta.getX());
            default -> throw new IllegalArgumentException("generic doors require a horizontal facing");
        };
    }

    private static AABB normalized(double minX, double minY, double minZ,
                                   double maxX, double maxY, double maxZ) {
        return new AABB(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ),
                Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
    }

    /** Exact {@code Rotation.getBlockRotation(dir)} plus the old X-axis flip. */
    private static BlockPos rotateForLegacyDoor(Direction facing, BlockPos local) {
        return switch (facing) {
            case NORTH -> local;
            case SOUTH -> new BlockPos(-local.getX(), local.getY(), -local.getZ());
            case EAST -> new BlockPos(-local.getZ(), local.getY(), local.getX());
            case WEST -> new BlockPos(local.getZ(), local.getY(), -local.getX());
            default -> throw new IllegalArgumentException("generic doors require a horizontal facing");
        };
    }
}
