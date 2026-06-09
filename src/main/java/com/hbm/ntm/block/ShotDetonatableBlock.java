package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface ShotDetonatableBlock {
    boolean detonateFromShot(Level level, BlockPos pos, BlockState state, @Nullable Entity shooter);
}
