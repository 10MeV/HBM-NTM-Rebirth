package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidNet;

/**
 * Legacy-name facade for the Fluid MK2 network.
 */
@Deprecated(forRemoval = false)
public class FluidNetMK2 extends HbmFluidNet {
    public long fluidTracker;

    public FluidNetMK2(FluidType type) {
        super(type);
    }

    public FluidNetMK2(FluidType type, long timeoutMs) {
        super(type, timeoutMs);
    }

    @Override
    public void resetTrackers() {
        super.resetTrackers();
        fluidTracker = 0L;
    }

    @Override
    public long update() {
        long transferred = super.update();
        fluidTracker = getFluidTracker();
        return transferred;
    }
}
