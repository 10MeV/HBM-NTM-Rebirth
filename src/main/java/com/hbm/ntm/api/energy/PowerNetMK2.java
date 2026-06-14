package com.hbm.ntm.api.energy;

/**
 * Modern API namespace alias for Energy MK2 networks.
 */
@Deprecated(forRemoval = false)
public class PowerNetMK2 extends com.hbm.ntm.energy.PowerNetMK2 {
    public PowerNetMK2() {
        super();
    }

    public PowerNetMK2(long timeoutMs) {
        super(timeoutMs);
    }
}
