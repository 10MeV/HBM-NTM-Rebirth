package com.hbm.ntm.energy;

public enum HbmEnergySideMode {
    NONE(false, false),
    INPUT(true, false),
    OUTPUT(false, true),
    BOTH(true, true);

    private final boolean canReceive;
    private final boolean canExtract;

    HbmEnergySideMode(boolean canReceive, boolean canExtract) {
        this.canReceive = canReceive;
        this.canExtract = canExtract;
    }

    public boolean canReceive() {
        return canReceive;
    }

    public boolean canExtract() {
        return canExtract;
    }
}
