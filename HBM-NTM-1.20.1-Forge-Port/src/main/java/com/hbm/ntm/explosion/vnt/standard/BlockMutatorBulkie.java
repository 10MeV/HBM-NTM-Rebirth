package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockMutatorBulkie implements BlockMutator {
    private final BlockState replacement;

    public BlockMutatorBulkie(Block block) {
        this(block.defaultBlockState());
    }

    public BlockMutatorBulkie(BlockState replacement) {
        this.replacement = replacement;
    }

    @Override
    public void mutatePre(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        if (!state.isCollisionShapeFullBlock(explosion.level(), pos)) {
            return;
        }
        Vec3 offset = Vec3.atCenterOf(pos).subtract(explosion.position());
        if (offset.length() >= explosion.size() - 0.5D) {
            explosion.level().setBlock(pos, replacement, 3);
        }
    }
}
