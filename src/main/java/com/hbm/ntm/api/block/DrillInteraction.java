package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public interface DrillInteraction {
    boolean canBreak(Level level, BlockPos pos, BlockState state, MiningDrill drill);

    ItemStack extractResource(Level level, BlockPos pos, BlockState state, MiningDrill drill);

    float getRelativeHardness(Level level, BlockPos pos, BlockState state, MiningDrill drill);
}
