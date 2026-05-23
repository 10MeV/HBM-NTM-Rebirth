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
    public void mutatePost(ExplosionVnt explosion, BlockPos pos) {
        if (explosion.level().getBlockState(pos).isCollisionShapeFullBlock(explosion.level(), pos)) {
            explosion.level().setBlock(pos, replacement, 3);
        }
    }
}
