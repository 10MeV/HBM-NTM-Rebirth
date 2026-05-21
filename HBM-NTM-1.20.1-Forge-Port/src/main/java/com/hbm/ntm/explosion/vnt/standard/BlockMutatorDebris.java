package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMutatorDebris implements BlockMutator {
    private final BlockState replacement;

    public BlockMutatorDebris(Block block) {
        this(block.defaultBlockState());
    }

    public BlockMutatorDebris(BlockState replacement) {
        this.replacement = replacement;
    }

    @Override
    public void mutatePost(ExplosionVnt explosion, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            BlockState neighborState = explosion.level().getBlockState(neighbor);
            if (neighborState.isCollisionShapeFullBlock(explosion.level(), neighbor) && !neighborState.is(replacement.getBlock())) {
                explosion.level().setBlock(pos, replacement, 3);
                return;
            }
        }
    }
}
