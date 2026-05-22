package com.hbm.ntm.fluid.trait;

public class PwrModeratorFluidTrait extends FluidTrait {
    private final double multiplier;

    public PwrModeratorFluidTrait(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
