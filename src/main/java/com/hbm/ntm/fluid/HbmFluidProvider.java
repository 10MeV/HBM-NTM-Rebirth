package com.hbm.ntm.fluid;

public interface HbmFluidProvider extends HbmFluidUser {
    void useUpFluid(FluidType type, int pressure, long amount);

    default long getProviderSpeed(FluidType type, int pressure) {
        return 1_000_000_000L;
    }

    long getFluidAvailable(FluidType type, int pressure);

    default int[] getProvidingPressureRange(FluidType type) {
        return DEFAULT_PRESSURE_RANGE;
    }
}
