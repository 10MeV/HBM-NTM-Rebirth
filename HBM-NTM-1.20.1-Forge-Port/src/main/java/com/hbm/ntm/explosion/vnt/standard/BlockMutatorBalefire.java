package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMutatorBalefire implements BlockMutator {
    private final boolean always;

    public BlockMutatorBalefire() {
        this(false);
    }

    public BlockMutatorBalefire(boolean always) {
        this.always = always;
    }

    @Override
    public void mutatePost(ExplosionVnt explosion, BlockPos pos) {
        if (explosion.level().getBlockState(pos).isAir()
                && explosion.level().getBlockState(pos.below()).isSolidRender(explosion.level(), pos.below())
                && (always || explosion.level().random.nextInt(3) == 0)) {
            explosion.level().setBlockAndUpdate(pos, balefireState());
        }
    }

    private static BlockState balefireState() {
        return ModBlocks.BALEFIRE.get().defaultBlockState();
    }
}
