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

    public BlockMutatorBulkie(Block block, int meta) {
        this(LegacyVntBlockStateMapper.fromLegacyMeta(block, meta));
    }

    public BlockMutatorBulkie(BlockState replacement) {
        this.replacement = replacement;
    }

    @Override
    public void mutatePre(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        if (explosion.level().isOutsideBuildHeight(pos)) {
            return;
        }
        if (!state.isSolidRender(explosion.level(), pos)) {
            return;
        }
        double threshold = explosion.size() - 0.5D;
        Vec3 origin = explosion.position();
        double dx = pos.getX() + 0.5D - origin.x;
        double dy = pos.getY() + 0.5D - origin.y;
        double dz = pos.getZ() + 0.5D - origin.z;
        if (threshold <= 0.0D || dx * dx + dy * dy + dz * dz >= threshold * threshold) {
            explosion.level().setBlock(pos, replacement, 3);
        }
    }
}
