package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class BlockMutatorLava implements BlockMutator {
    private final boolean always;

    public BlockMutatorLava() {
        this(false);
    }

    public BlockMutatorLava(boolean always) {
        this.always = always;
    }

    @Override
    public void mutatePost(ExplosionVnt explosion, BlockPos pos) {
        if (explosion.level().getBlockState(pos).isAir()
                && explosion.level().getBlockState(pos.below()).isSolidRender(explosion.level(), pos.below())
                && (always || explosion.level().random.nextInt(3) == 0)) {
            explosion.level().setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        }
    }
}
