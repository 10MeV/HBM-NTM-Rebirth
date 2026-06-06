package com.hbm.ntm.api.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public interface IEnterableBlock {
    boolean canItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity);

    void onItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity);

    boolean canPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity);

    void onPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity);
}
