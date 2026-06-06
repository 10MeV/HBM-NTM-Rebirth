package com.hbm.ntm.explosion.vnt.interfaces;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface DropChanceMutator {
    float mutateDropChance(ExplosionVnt explosion, BlockState state, BlockPos pos, float chance);
}
