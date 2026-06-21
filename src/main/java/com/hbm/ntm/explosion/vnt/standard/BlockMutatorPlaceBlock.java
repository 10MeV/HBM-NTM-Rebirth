package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMutatorPlaceBlock implements BlockMutator {
    private final BlockState replacement;

    public BlockMutatorPlaceBlock(BlockState replacement) {
        this.replacement = replacement;
    }

    @Override
    public void mutatePre(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        if (explosion.level().isOutsideBuildHeight(pos)) {
            return;
        }
        if (state.isSolidRender(explosion.level(), pos)) {
            explosion.level().setBlock(pos, replacement, 3);
        }
    }
}
