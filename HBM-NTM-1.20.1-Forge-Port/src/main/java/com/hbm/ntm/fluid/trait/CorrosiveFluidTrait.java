package com.hbm.ntm.fluid.trait;

public class CorrosiveFluidTrait extends FluidTrait {
    private final int rating;

    public CorrosiveFluidTrait(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public boolean isHighlyCorrosive() {
        return rating > 50;
    }
}
