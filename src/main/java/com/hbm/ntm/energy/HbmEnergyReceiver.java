package com.hbm.ntm.energy;

public interface HbmEnergyReceiver extends HbmEnergyHandler {
    enum ConnectionPriority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST
    }

    /**
     * Receives HE and returns the remainder that could not fit.
     */
    default long transferPower(long power) {
        if (power <= 0L) {
            return 0L;
        }
        long accepted = Math.min(power, Math.max(0L, getMaxPower() - getPower()));
        if (accepted > 0L) {
            setPower(getPower() + accepted);
        }
        return power - accepted;
    }

    default long getReceiverSpeed() {
        return getMaxPower();
    }

    default boolean allowDirectProvision() {
        return true;
    }

    default ConnectionPriority getPriority() {
        return ConnectionPriority.NORMAL;
    }
}
