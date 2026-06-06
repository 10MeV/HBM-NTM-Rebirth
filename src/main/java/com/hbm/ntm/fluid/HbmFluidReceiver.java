package com.hbm.ntm.fluid;

import com.hbm.ntm.energy.HbmEnergyReceiver;

public interface HbmFluidReceiver extends HbmFluidUser {
    /**
     * Receives fluid and returns the remainder that could not fit.
     */
    long transferFluid(FluidType type, int pressure, long amount);

    default long getReceiverSpeed(FluidType type, int pressure) {
        return 1_000_000_000L;
    }

    long getDemand(FluidType type, int pressure);

    default int[] getReceivingPressureRange(FluidType type) {
        return DEFAULT_PRESSURE_RANGE;
    }

    default HbmEnergyReceiver.ConnectionPriority getFluidPriority() {
        return HbmEnergyReceiver.ConnectionPriority.NORMAL;
    }
}
