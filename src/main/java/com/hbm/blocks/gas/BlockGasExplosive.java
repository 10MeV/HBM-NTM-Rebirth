package com.hbm.blocks.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Source-backed 1.7.10 explosive gas behavior.
 */
public class BlockGasExplosive extends BlockGasFlammable {
    @Override
    protected void combust(Level level, BlockPos pos) {
        super.combust(level, pos);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                3.0F, true, Level.ExplosionInteraction.NONE);
    }
}
