package com.hbm.ntm.neutron;

public interface RBMKNeutronColumn {
    RBMKNeutronHandler.RBMKType getRBMKType();

    boolean hasRBMKLid();

    default boolean isRBMKModerated() {
        return false;
    }
}
