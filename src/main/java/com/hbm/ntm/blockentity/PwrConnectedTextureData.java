package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * Client CTContext equivalent for the assembled PWR shell.  The legacy
 * {@code BlockPWR#canConnect} accepts exactly another {@code pwr_block} or
 * the controller, including diagonal neighbours used by every face fragment.
 */
public final class PwrConnectedTextureData {
    public static final ModelProperty<Long> CONNECTION_MASK = new ModelProperty<>();

    private static final int[][][] OFFSETS = {
            {{-1, 0, 1}, {0, 0, 1}, {1, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {-1, 0, -1}, {0, 0, -1}, {1, 0, -1}},
            {{-1, 0, -1}, {0, 0, -1}, {1, 0, -1}, {-1, 0, 0}, {1, 0, 0}, {-1, 0, 1}, {0, 0, 1}, {1, 0, 1}},
            {{1, 1, 0}, {0, 1, 0}, {-1, 1, 0}, {1, 0, 0}, {-1, 0, 0}, {1, -1, 0}, {0, -1, 0}, {-1, -1, 0}},
            {{-1, 1, 0}, {0, 1, 0}, {1, 1, 0}, {-1, 0, 0}, {1, 0, 0}, {-1, -1, 0}, {0, -1, 0}, {1, -1, 0}},
            {{0, 1, -1}, {0, 1, 0}, {0, 1, 1}, {0, 0, -1}, {0, 0, 1}, {0, -1, -1}, {0, -1, 0}, {0, -1, 1}},
            {{0, 1, 1}, {0, 1, 0}, {0, 1, -1}, {0, 0, 1}, {0, 0, -1}, {0, -1, 1}, {0, -1, 0}, {0, -1, -1}}
    };

    private PwrConnectedTextureData() {
    }

    public static long connectionMask(Level level, BlockPos pos) {
        if (level == null) return 0L;
        long mask = 0L;
        for (Direction face : Direction.values()) {
            for (int neighbor = 0; neighbor < 8; neighbor++) {
                int[] offset = OFFSETS[face.ordinal()][neighbor];
                if (level.getBlockState(pos.offset(offset[0], offset[1], offset[2])).is(ModBlocks.PWR_BLOCK.get())
                        || level.getBlockState(pos.offset(offset[0], offset[1], offset[2])).is(ModBlocks.PWR_CONTROLLER.get())) {
                    mask |= 1L << (face.ordinal() * 8 + neighbor);
                }
            }
        }
        return mask;
    }
}
