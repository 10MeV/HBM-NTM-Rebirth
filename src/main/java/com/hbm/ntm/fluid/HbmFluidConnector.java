package com.hbm.ntm.fluid;

import net.minecraft.core.Direction;

public interface HbmFluidConnector {
    default boolean canConnectFluid(FluidType type, Direction side) {
        return type != null && type != HbmFluids.NONE && side != null;
    }
}
