package com.hbm.ntm.energy;

/**
 * Legacy-name facade for the Energy MK2 network.
 */
@Deprecated(forRemoval = false)
public class PowerNetMK2 extends HbmPowerNet {
    public long energyTracker;

    public PowerNetMK2() {
        super();
    }

    public PowerNetMK2(long timeoutMs) {
        super(timeoutMs);
    }

    @Override
    public void resetTrackers() {
        super.resetTrackers();
        energyTracker = 0L;
    }

    @Override
    public long update() {
        long transferred = super.update();
        energyTracker = getEnergyTracker();
        return transferred;
    }

    @Override
    public long sendPowerDiode(long power) {
        long remainder = super.sendPowerDiode(power);
        energyTracker = getEnergyTracker();
        return remainder;
    }
}
