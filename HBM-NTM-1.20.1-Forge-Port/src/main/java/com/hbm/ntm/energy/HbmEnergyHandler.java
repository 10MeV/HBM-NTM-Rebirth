package com.hbm.ntm.energy;

/**
 * Base HBM HE energy contract, preserving the 1.7.10 long-valued storage model.
 */
public interface HbmEnergyHandler {
    long getPower();

    void setPower(long power);

    long getMaxPower();

    default long clampPower(long power) {
        return Math.max(0L, Math.min(power, getMaxPower()));
    }

    default void setPowerClamped(long power) {
        setPower(clampPower(power));
    }

    default boolean hasPower(long amount) {
        return getPower() >= amount;
    }
}
