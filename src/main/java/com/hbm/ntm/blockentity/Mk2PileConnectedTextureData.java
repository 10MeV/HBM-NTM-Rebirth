package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.Mk2PileStructureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * The 1.7.10 CTContext neighborhood contract for {@code BlockPile}.  Each byte is
 * ordered TL, TC, TR, CL, CR, BL, BC, BR for one {@link Direction} ordinal.
 */
public final class Mk2PileConnectedTextureData {
    public static final ModelProperty<Long> CONNECTION_MASK = new ModelProperty<>();

    private static final int[][][] OFFSETS = {
            {{-1, 0, 1}, {0, 0, 1}, {1, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {-1, 0, -1}, {0, 0, -1}, {1, 0, -1}},
            {{-1, 0, -1}, {0, 0, -1}, {1, 0, -1}, {-1, 0, 0}, {1, 0, 0}, {-1, 0, 1}, {0, 0, 1}, {1, 0, 1}},
            {{1, 1, 0}, {0, 1, 0}, {-1, 1, 0}, {1, 0, 0}, {-1, 0, 0}, {1, -1, 0}, {0, -1, 0}, {-1, -1, 0}},
            {{-1, 1, 0}, {0, 1, 0}, {1, 1, 0}, {-1, 0, 0}, {1, 0, 0}, {-1, -1, 0}, {0, -1, 0}, {1, -1, 0}},
            {{0, 1, -1}, {0, 1, 0}, {0, 1, 1}, {0, 0, -1}, {0, 0, 1}, {0, -1, -1}, {0, -1, 0}, {0, -1, 1}},
            {{0, 1, 1}, {0, 1, 0}, {0, 1, -1}, {0, 0, 1}, {0, 0, -1}, {0, -1, 1}, {0, -1, 0}, {0, -1, -1}}
    };

    private Mk2PileConnectedTextureData() {
    }

    public static long connectionMask(Level level, BlockPos pos) {
        if (level == null) return 0L;
        long mask = 0L;
        for (Direction face : Direction.values()) {
            for (int neighbor = 0; neighbor < 8; neighbor++) {
                int[] offset = OFFSETS[face.ordinal()][neighbor];
                if (level.getBlockState(pos.offset(offset[0], offset[1], offset[2])).getBlock()
                        instanceof Mk2PileStructureBlock) {
                    mask |= 1L << (face.ordinal() * 8 + neighbor);
                }
            }
        }
        return mask;
    }
}
