package com.hbm.ntm.api.common;

/**
 * Stable HBM-side contract for the legacy OpenComputers component surface.
 *
 * <p>The 1.7.10 OpenComputers API is not binary-compatible with modern OC
 * implementations.  A target-specific adapter may therefore delegate to this
 * interface without making the block entity depend on a particular OC fork.</p>
 */
public interface OpenComputersComponent {
    String getComponentName();

    String[] methods();

    Object[] invoke(String method) throws NoSuchMethodException;
}
