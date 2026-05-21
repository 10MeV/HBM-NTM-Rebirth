package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class CompositeBlockMutator implements BlockMutator {
    private final List<BlockMutator> mutators = new ArrayList<>();

    public CompositeBlockMutator add(BlockMutator mutator) {
        if (mutator != null) {
            mutators.add(mutator);
        }
        return this;
    }

    public boolean isEmpty() {
        return mutators.isEmpty();
    }

    @Override
    public void mutatePre(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        for (BlockMutator mutator : mutators) {
            mutator.mutatePre(explosion, state, pos);
        }
    }

    @Override
    public void mutatePost(ExplosionVnt explosion, BlockPos pos) {
        for (BlockMutator mutator : mutators) {
            mutator.mutatePost(explosion, pos);
        }
    }
}
