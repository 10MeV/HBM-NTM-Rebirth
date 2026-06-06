package com.hbm.ntm.fluid;

public record HbmFluidStack(FluidType type, int amount, int pressure) {
    public HbmFluidStack {
        if (type == null) {
            type = HbmFluids.NONE;
        }
        amount = Math.max(0, amount);
        pressure = HbmFluidTank.clampPressure(pressure);
    }

    public HbmFluidStack(FluidType type, int amount) {
        this(type, amount, 0);
    }

    public boolean isEmpty() {
        return type == HbmFluids.NONE || amount <= 0;
    }
}
