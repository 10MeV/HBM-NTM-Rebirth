package com.hbm.ntm.fluid.trait;

public class FlammableFluidTrait extends FluidTrait {
    private final long heatEnergyPerBucket;

    public FlammableFluidTrait(long heatEnergyPerBucket) {
        this.heatEnergyPerBucket = heatEnergyPerBucket;
    }

    public long getHeatEnergyPerBucket() {
        return heatEnergyPerBucket;
    }
}
