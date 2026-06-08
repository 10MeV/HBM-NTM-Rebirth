package com.hbm.ntm.neutron;

public interface RBMKRodColumn extends RBMKFluxReceiver {
    boolean hasFuelRod();

    double lastFluxQuantity();
}
