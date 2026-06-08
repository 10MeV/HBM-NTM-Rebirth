package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public interface Blowable {
    void applyFan(Level level, BlockPos pos, Direction direction, int distance);
}
