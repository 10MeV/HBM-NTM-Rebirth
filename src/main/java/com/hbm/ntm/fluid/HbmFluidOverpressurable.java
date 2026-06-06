package com.hbm.ntm.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Receiver-side hook for the legacy Fluid MK2 overpressure dispatch.
 */
public interface HbmFluidOverpressurable {
    void explodeFromFluidOverpressure(Level level, BlockPos pos);
}
