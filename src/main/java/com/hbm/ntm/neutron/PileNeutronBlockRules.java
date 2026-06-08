package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface PileNeutronBlockRules {
    PileNeutronBlockRules PASS = (level, pos, state, blockEntity) -> PileNeutronBlockResult.pass();

    /**
     * The block entity may be null when the modern block has no block entity but still has a legacy neutron rule.
     */
    PileNeutronBlockResult evaluate(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity);
}
