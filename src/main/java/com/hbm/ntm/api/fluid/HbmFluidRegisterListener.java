package com.hbm.ntm.api.fluid;

/**
 * Legacy source-compatibility hook. Modern HBM fluid types are built from the
 * fixed table and config/datapack-facing JSON, so this listener is intentionally
 * not invoked by the compat facade.
 */
@FunctionalInterface
public interface HbmFluidRegisterListener {
    void onFluidsLoad();
}
