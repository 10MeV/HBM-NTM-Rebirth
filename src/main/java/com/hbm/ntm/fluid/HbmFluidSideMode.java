package com.hbm.ntm.fluid;

public enum HbmFluidSideMode {
    NONE(false, false),
    INPUT(true, false),
    OUTPUT(false, true),
    BOTH(true, true);

    private final boolean canFill;
    private final boolean canDrain;

    HbmFluidSideMode(boolean canFill, boolean canDrain) {
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    public boolean canFill() {
        return canFill;
    }

    public boolean canDrain() {
        return canDrain;
    }
}
