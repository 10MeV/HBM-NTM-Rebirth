package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy extent order is posX, negX, posY, negY, posZ, negZ.
 */
public record MultiblockExtents(int posX, int negX, int posY, int negY, int posZ, int negZ) {
    public static MultiblockExtents ofLegacy(int[] extents) {
        requireSix(extents, "Legacy multiblock extents");
        return new MultiblockExtents(extents[0], extents[1], extents[2], extents[3], extents[4], extents[5]);
    }

    /**
     * Legacy BlockDummyable/MultiblockHandlerXR dimensions are ordered as U, D, N, S, W, E
     * and authored for SOUTH. Rotate first, then convert into this record's axis order.
     */
    public static MultiblockExtents ofLegacyXr(int[] dimensions, Direction facing) {
        int[] rotated = rotateLegacyXr(dimensions, facing);
        return new MultiblockExtents(rotated[5], rotated[4], rotated[0], rotated[1], rotated[3], rotated[2]);
    }

    public static int[] rotateLegacyXr(int[] dimensions, Direction facing) {
        requireSix(dimensions, "Legacy XR multiblock dimensions");
        Direction horizontal = facing == null || facing.getAxis().isVertical() ? Direction.SOUTH : facing;
        return switch (horizontal) {
            case NORTH -> new int[] { dimensions[0], dimensions[1], dimensions[3], dimensions[2], dimensions[5], dimensions[4] };
            case EAST -> new int[] { dimensions[0], dimensions[1], dimensions[5], dimensions[4], dimensions[2], dimensions[3] };
            case WEST -> new int[] { dimensions[0], dimensions[1], dimensions[4], dimensions[5], dimensions[3], dimensions[2] };
            default -> dimensions.clone();
        };
    }

    public int[] toLegacyArray() {
        return new int[] { posX, negX, posY, negY, posZ, negZ };
    }

    public int[] toLegacyXrArray() {
        return new int[] { posY, negY, negZ, posZ, negX, posX };
    }

    public List<BlockPos> offsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int x = -negX; x <= posX; x++) {
            for (int y = -negY; y <= posY; y++) {
                for (int z = -negZ; z <= posZ; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        offsets.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return offsets;
    }

    private static void requireSix(int[] values, String name) {
        if (values == null || values.length != 6) {
            throw new IllegalArgumentException(name + " must contain exactly six values");
        }
    }
}
