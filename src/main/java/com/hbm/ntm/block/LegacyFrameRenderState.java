package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class LegacyFrameRenderState {
    public static final BooleanProperty FRAME = BooleanProperty.create("frame");

    private LegacyFrameRenderState() {
    }

    public static BlockState syncFrameBlockState(Level level, BlockPos pos, BlockState state, int yOffset) {
        if (level == null || level.isClientSide || !state.hasProperty(FRAME)) {
            return state;
        }
        boolean visible = hasFrameSupport(level, pos, yOffset);
        if (state.getValue(FRAME) == visible) {
            return state;
        }
        BlockState updated = state.setValue(FRAME, visible);
        level.setBlock(pos, updated, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS);
        return updated;
    }

    public static boolean isFrameVisible(BlockState state, Level level, BlockPos pos, int yOffset) {
        if (state.hasProperty(FRAME)) {
            return state.getValue(FRAME);
        }
        return hasFrameSupport(level, pos, yOffset);
    }

    private static boolean hasFrameSupport(Level level, BlockPos pos, int yOffset) {
        return level != null && !level.getBlockState(pos.above(yOffset)).isAir();
    }
}
