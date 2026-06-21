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

    public BlockMutatorDebris(Block block, int meta) {
        this(LegacyVntBlockStateMapper.fromLegacyMeta(block, meta));
    }

    public BlockMutatorDebris(BlockState replacement) {
        this.replacement = replacement;
    }

    @Override
    public void mutatePost(ExplosionVnt explosion, BlockPos pos) {
        if (explosion.level().isOutsideBuildHeight(pos)) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (explosion.level().isOutsideBuildHeight(neighbor)) {
                continue;
            }
            BlockState neighborState = explosion.level().getBlockState(neighbor);
            if (neighborState.isSolidRender(explosion.level(), neighbor) && !neighborState.equals(replacement)) {
                explosion.level().setBlock(pos, replacement, 3);
                return;
            }
        }
    }
}
