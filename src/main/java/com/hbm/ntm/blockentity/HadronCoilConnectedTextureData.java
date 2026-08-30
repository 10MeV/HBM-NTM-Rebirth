package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HadronCoilBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * Legacy {@code BlockHadronCoil#canConnect} accepts every Hadron coil tier.
 * The byte order is TL, TC, TR, CL, CR, BL, BC, BR for each face ordinal.
 */
public final class HadronCoilConnectedTextureData {
    public static final ModelProperty<Long> CONNECTION_MASK = new ModelProperty<>();

    private static final int[][][] OFFSETS = {
            {{-1, 0, 1}, {0, 0, 1}, {1, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {-1, 0, -1}, {0, 0, -1}, {1, 0, -1}},
            {{-1, 0, -1}, {0, 0, -1}, {1, 0, -1}, {-1, 0, 0}, {1, 0, 0}, {-1, 0, 1}, {0, 0, 1}, {1, 0, 1}},
            {{1, 1, 0}, {0, 1, 0}, {-1, 1, 0}, {1, 0, 0}, {-1, 0, 0}, {1, -1, 0}, {0, -1, 0}, {-1, -1, 0}},
            {{-1, 1, 0}, {0, 1, 0}, {1, 1, 0}, {-1, 0, 0}, {1, 0, 0}, {-1, -1, 0}, {0, -1, 0}, {1, -1, 0}},
            {{0, 1, -1}, {0, 1, 0}, {0, 1, 1}, {0, 0, -1}, {0, 0, 1}, {0, -1, -1}, {0, -1, 0}, {0, -1, 1}},
            {{0, 1, 1}, {0, 1, 0}, {0, 1, -1}, {0, 0, 1}, {0, 0, -1}, {0, -1, 1}, {0, -1, 0}, {0, -1, -1}}
    };

    private HadronCoilConnectedTextureData() {
    }

    public static long connectionMask(BlockAndTintGetter level, BlockPos pos) {
        long mask = 0L;
        for (Direction face : Direction.values()) {
            for (int neighbor = 0; neighbor < 8; neighbor++) {
                int[] offset = OFFSETS[face.ordinal()][neighbor];
                if (level.getBlockState(pos.offset(offset[0], offset[1], offset[2])).getBlock() instanceof HadronCoilBlock) {
                    mask |= 1L << (face.ordinal() * 8 + neighbor);
                }
            }
        }
        return mask;
    }
}
