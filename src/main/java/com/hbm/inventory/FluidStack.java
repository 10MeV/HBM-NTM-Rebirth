package com.hbm.inventory;

import com.hbm.ntm.fluid.FluidType;

/**
 * Legacy package facade for the 1.7.10 fluid stack tuple.
 */
@Deprecated(forRemoval = false)
public class FluidStack {
    public FluidType type;
    public int fill;
    public int pressure;

    @Deprecated
    public FluidStack(int fill, FluidType type) {
        this(type, fill, 0);
    }

    public FluidStack(FluidType type, int fill) {
        this(type, fill, 0);
    }

    public FluidStack(FluidType type, int fill, int pressure) {
        this.type = type;
        this.fill = fill;
        this.pressure = pressure;
    }

    public com.hbm.ntm.fluid.HbmFluidStack toModern() {
        return new com.hbm.ntm.fluid.HbmFluidStack(type, fill, pressure);
    }
}
