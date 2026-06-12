package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnector;
import net.minecraft.core.Direction;

/**
 * Legacy-name bridge for the Fluid MK2 connector API.
 */
@Deprecated(forRemoval = false)
public interface IFluidConnectorMK2 extends HbmFluidConnector {
    default boolean canConnect(FluidType type, Direction side) {
        return side != null;
    }

    @Override
    default boolean canConnectFluid(FluidType type, Direction side) {
        return canConnect(type, side);
    }
}
