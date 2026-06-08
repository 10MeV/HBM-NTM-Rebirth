package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface PileNeutronBlockBehavior {
    PileNeutronBlockResult evaluatePileNeutronBlock(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity);
}
