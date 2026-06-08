package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface PileNeutronRadiationHandler {
    PileNeutronRadiationHandler NOOP = (level, pos, radiation) -> {
    };

    void radiateEntities(Level level, BlockPos pos, float radiation);
}
