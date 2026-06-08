package com.hbm.ntm.api.fluid;

/**
 * Called every time the HBM fluid type table is rebuilt. Register addon or
 * compat fluid types here so they survive in-game reloads.
 */
@FunctionalInterface
public interface HbmFluidRegisterListener {
    void onFluidsLoad();
}
