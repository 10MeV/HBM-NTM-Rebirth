package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMutatorDigamma implements BlockMutator {
    private final boolean circuit;

    public BlockMutatorDigamma(boolean circuit) {
        this.circuit = circuit;
    }

    @Override
    public void mutatePost(ExplosionVnt explosion, BlockPos pos) {
        if (!explosion.level().getBlockState(pos).isCollisionShapeFullBlock(explosion.level(), pos)) {
            return;
        }

        BlockState replacement = ashState();
        if (circuit && shouldPlaceCircuitDebris(explosion, pos)) {
            replacement = ModBlocks.PRIBRIS_DIGAMMA.get().defaultBlockState();
        }

        explosion.level().setBlock(pos, replacement, 3);

        BlockPos above = pos.above();
        BlockState fire = ModBlocks.FIRE_DIGAMMA.get().defaultBlockState();
        if (explosion.level().random.nextInt(5) == 0
                && explosion.level().getBlockState(above).isAir()
                && fire.canSurvive(explosion.level(), above)) {
            explosion.level().setBlock(above, fire, 3);
        }
    }

    private boolean shouldPlaceCircuitDebris(ExplosionVnt explosion, BlockPos pos) {
        if (Math.floorMod(pos.getX(), 3) == 0 && Math.floorMod(pos.getZ(), 3) == 0) {
            return true;
        }
        return (Math.floorMod(pos.getX(), 3) == 0 || Math.floorMod(pos.getZ(), 3) == 0)
                && explosion.level().random.nextBoolean();
    }

    private BlockState ashState() {
        return ModBlocks.ASH_DIGAMMA.get().defaultBlockState();
    }
}
