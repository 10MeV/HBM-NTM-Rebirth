package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import com.hbm.ntm.explosion.vnt.interfaces.BlockProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BlockProcessorNoDamage implements BlockProcessor {
    private BlockMutator mutator;

    public BlockProcessorNoDamage withBlockEffect(BlockMutator mutator) {
        this.mutator = mutator;
        return this;
    }

    @Override
    public void process(ExplosionVnt explosion, ServerLevel level, Vec3 position, Set<BlockPos> affectedBlocks) {
        if (mutator != null) {
            for (BlockPos pos : affectedBlocks) {
                if (level.isOutsideBuildHeight(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!state.isAir()) {
                    mutator.mutatePre(explosion, state, pos);
                }
            }
            for (BlockPos pos : affectedBlocks) {
                if (!level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir()) {
                    mutator.mutatePost(explosion, pos);
                }
            }
        }
        affectedBlocks.clear();
        explosion.compat().clearToBlow();
    }
}
