package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidProvider;

/**
 * Legacy-name bridge for Fluid MK2 providers.
 */
@Deprecated(forRemoval = false)
public interface IFluidProviderMK2 extends IFluidUserMK2, HbmFluidProvider {
    @Override
    void useUpFluid(FluidType type, int pressure, long amount);

    @Override
    long getFluidAvailable(FluidType type, int pressure);

    @Override
    default long getProviderSpeed(FluidType type, int pressure) {
        return HbmFluidProvider.super.getProviderSpeed(type, pressure);
    }

    @Override
    default int[] getProvidingPressureRange(FluidType type) {
        return IFluidUserMK2.DEFAULT_PRESSURE_RANGE;
    }
}
