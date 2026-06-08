package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class RBMKFuelRodState {
    public static final String TAG_YIELD = "yield";
    public static final String TAG_XENON = "xenon";
    public static final String TAG_CORE_HEAT = "core";
    public static final String TAG_HULL_HEAT = "hull";
    public static final double DEFAULT_HEAT = 20.0D;

    private double remainingYield;
    private double xenon;
    private double coreHeat = DEFAULT_HEAT;
    private double hullHeat = DEFAULT_HEAT;

    public static RBMKFuelRodState fresh(RBMKFuelRodSpec spec) {
        RBMKFuelRodState state = new RBMKFuelRodState();
        state.setRemainingYield(spec.totalYield());
        state.setCoreHeat(DEFAULT_HEAT);
        state.setHullHeat(DEFAULT_HEAT);
        return state;
    }

    public double remainingYield() {
        return remainingYield;
    }

    public void setRemainingYield(double remainingYield) {
        this.remainingYield = Math.max(0.0D, remainingYield);
    }

    public double xenon() {
        return xenon;
    }

    public void setXenon(double xenon) {
        this.xenon = clamp(xenon, 0.0D, 100.0D);
    }

    public double xenonLevel() {
        return xenon / 100.0D;
    }

    public double coreHeat() {
        return coreHeat;
    }

    public void setCoreHeat(double coreHeat) {
        this.coreHeat = rectify(coreHeat);
    }

    public double hullHeat() {
        return hullHeat;
    }

    public void setHullHeat(double hullHeat) {
        this.hullHeat = rectify(hullHeat);
    }

    public double enrichment(RBMKFuelRodSpec spec) {
        return spec.totalYield() <= 0.0D ? 0.0D : remainingYield / spec.totalYield();
    }

    public void load(CompoundTag tag, RBMKFuelRodSpec spec) {
        setRemainingYield(tag.contains(TAG_YIELD) ? tag.getDouble(TAG_YIELD) : spec.totalYield());
        setXenon(tag.getDouble(TAG_XENON));
        setCoreHeat(tag.contains(TAG_CORE_HEAT) ? tag.getDouble(TAG_CORE_HEAT) : DEFAULT_HEAT);
        setHullHeat(tag.contains(TAG_HULL_HEAT) ? tag.getDouble(TAG_HULL_HEAT) : DEFAULT_HEAT);
    }

    public void save(CompoundTag tag) {
        tag.putDouble(TAG_YIELD, remainingYield);
        tag.putDouble(TAG_XENON, xenon);
        tag.putDouble(TAG_CORE_HEAT, coreHeat);
        tag.putDouble(TAG_HULL_HEAT, hullHeat);
    }

    static double rectify(double value) {
        if (value > 1_000_000.0D) {
            return 1_000_000.0D;
        }
        if (value < DEFAULT_HEAT || Double.isNaN(value)) {
            return DEFAULT_HEAT;
        }
        return value;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min || Double.isNaN(value)) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
