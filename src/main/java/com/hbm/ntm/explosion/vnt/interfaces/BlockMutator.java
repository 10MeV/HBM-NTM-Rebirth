package com.hbm.ntm.explosion.vnt.interfaces;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockMutator {
    default boolean suppressDrops(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        return false;
    }

    default void mutatePre(ExplosionVnt explosion, BlockState state, BlockPos pos) {
    }

    default void mutatePost(ExplosionVnt explosion, BlockPos pos) {
    }
}
