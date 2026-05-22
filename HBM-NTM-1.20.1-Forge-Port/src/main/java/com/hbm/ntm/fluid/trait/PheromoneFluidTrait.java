package com.hbm.ntm.fluid.trait;

public class PheromoneFluidTrait extends FluidTrait {
    private final int type;

    public PheromoneFluidTrait(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
