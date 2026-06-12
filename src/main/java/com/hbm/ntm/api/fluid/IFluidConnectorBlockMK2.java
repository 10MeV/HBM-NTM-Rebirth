package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

/**
 * Legacy-name bridge for block-level Fluid MK2 connection checks.
 */
@Deprecated(forRemoval = false)
public interface IFluidConnectorBlockMK2 extends HbmFluidConnectorBlock {
    default boolean canConnect(FluidType type, BlockGetter level, BlockPos pos, Direction side) {
        return side != null;
    }

    @Override
    default boolean canConnectFluid(BlockGetter level, BlockPos pos, FluidType type, Direction side) {
        return canConnect(type, level, pos, side);
    }
}
