package com.hbm.ntm.fluid.trait;

public class PoisonFluidTrait extends FluidTrait {
    private final boolean withering;
    private final int level;

    public PoisonFluidTrait(boolean withering, int level) {
        this.withering = withering;
        this.level = level;
    }

    public boolean isWithering() {
        return withering;
    }

    public int getLevel() {
        return level;
    }
}
