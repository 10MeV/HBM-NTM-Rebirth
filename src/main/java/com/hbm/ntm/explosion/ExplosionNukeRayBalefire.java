package com.hbm.ntm.explosion;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ExplosionNukeRayBalefire extends ExplosionNukeRayBatched {
    public ExplosionNukeRayBalefire(Level level, int x, int y, int z, int strength, int speed, int length) {
        super(level, x, y, z, strength, speed, length);
    }

    @Override
    protected void handleTip(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos below = pos.below();
        if (level.random.nextInt(5) == 0 && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            level.setBlock(pos, balefireState(), 3);
        } else {
            LegacyExplosionFluidCleanup.clearBlockOrLegacyLiquidNeighborhood(level, pos, 3);
        }
    }

    private BlockState balefireState() {
        return ModBlocks.BALEFIRE.get().defaultBlockState();
    }
}
