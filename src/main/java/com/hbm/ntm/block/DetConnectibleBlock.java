package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface DetConnectibleBlock {
    default boolean canConnectToDetCord(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        return true;
    }
}
