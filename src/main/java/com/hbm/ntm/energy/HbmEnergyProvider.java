package com.hbm.ntm.energy;

public interface HbmEnergyProvider extends HbmEnergyHandler {
    /**
     * Consumes available HE and returns the amount actually consumed.
     */
    default long usePower(long power) {
        if (power <= 0L) {
            return 0L;
        }
        long used = Math.min(power, getPower());
        if (used > 0L) {
            setPower(getPower() - used);
        }
        return used;
    }

    default long getProviderSpeed() {
        return getMaxPower();
    }
}
