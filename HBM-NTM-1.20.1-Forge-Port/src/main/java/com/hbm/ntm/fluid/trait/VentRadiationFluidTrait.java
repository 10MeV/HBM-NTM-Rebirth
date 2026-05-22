package com.hbm.ntm.fluid.trait;

public class VentRadiationFluidTrait extends FluidTrait {
    private final float radiationPerMb;

    public VentRadiationFluidTrait(float radiationPerMb) {
        this.radiationPerMb = radiationPerMb;
    }

    public float getRadiationPerMb() {
        return radiationPerMb;
    }
}
