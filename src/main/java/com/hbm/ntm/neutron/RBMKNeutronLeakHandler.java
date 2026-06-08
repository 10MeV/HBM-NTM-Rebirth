package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface RBMKNeutronLeakHandler {
    RBMKNeutronLeakHandler NOOP = (level, pos, radiation) -> {
    };

    void leak(Level level, BlockPos pos, float radiation);
}
