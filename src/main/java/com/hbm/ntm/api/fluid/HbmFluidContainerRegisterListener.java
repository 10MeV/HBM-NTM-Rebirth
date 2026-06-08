package com.hbm.ntm.api.fluid;

/**
 * Called after the HBM fluid type table is rebuilt. Register fixed full/empty
 * item fluid containers here so machine slots and fluid-container ingredients
 * can resolve them after reloads.
 */
@FunctionalInterface
public interface HbmFluidContainerRegisterListener {
    void onFluidContainersLoad();
}
