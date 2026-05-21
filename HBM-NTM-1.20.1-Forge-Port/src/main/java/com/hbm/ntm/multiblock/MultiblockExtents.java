package com.hbm.ntm.multiblock;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy extent order is posX, negX, posY, negY, posZ, negZ.
 */
public record MultiblockExtents(int posX, int negX, int posY, int negY, int posZ, int negZ) {
    public static MultiblockExtents ofLegacy(int[] extents) {
        if (extents == null || extents.length != 6) {
            throw new IllegalArgumentException("Legacy multiblock extents must contain exactly six values");
        }
        return new MultiblockExtents(extents[0], extents[1], extents[2], extents[3], extents[4], extents[5]);
    }

    public int[] toLegacyArray() {
        return new int[] { posX, negX, posY, negY, posZ, negZ };
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
}
