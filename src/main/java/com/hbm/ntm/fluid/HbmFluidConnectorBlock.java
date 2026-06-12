package com.hbm.ntm.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public interface HbmFluidConnectorBlock {
    default boolean canConnectFluid(BlockGetter level, BlockPos pos, FluidType type, Direction side) {
        return side != null;
    }
}
