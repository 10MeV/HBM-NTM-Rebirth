package com.hbm.ntm.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface Laserable {
    void addEnergy(Level level, BlockPos pos, long energy, @Nullable Direction side);
}
