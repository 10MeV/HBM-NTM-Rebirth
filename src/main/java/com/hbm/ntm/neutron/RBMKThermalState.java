package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class RBMKThermalState {
    public static final String TAG_HEAT = "heat";
    public static final String TAG_REASIM_WATER = "reasimWater";
    public static final String TAG_REASIM_STEAM = "reasimSteam";
    public static final int MAX_WATER = 16_000;
    public static final int MAX_STEAM = 16_000;
    public static final double MIN_PASSIVE_HEAT = 20.0D;

    private double heat;
    private int reasimWater;
    private int reasimSteam;
    private int craneIndicator;

    public double heat() {
        return heat;
    }

    public void setHeat(double heat) {
        this.heat = Math.max(0.0D, heat);
    }

    public int reasimWater() {
        return reasimWater;
    }

    public void setReasimWater(int reasimWater) {
        this.reasimWater = clamp(reasimWater, 0, MAX_WATER);
    }

    public int reasimSteam() {
        return reasimSteam;
    }

    public void setReasimSteam(int reasimSteam) {
        this.reasimSteam = clamp(reasimSteam, 0, MAX_STEAM);
    }

    public int craneIndicator() {
        return craneIndicator;
    }

    public void setCraneIndicator(int craneIndicator) {
        this.craneIndicator = Math.max(0, craneIndicator);
    }

    public void load(CompoundTag tag) {
        setHeat(tag.getDouble(TAG_HEAT));
        setReasimWater(tag.getInt(TAG_REASIM_WATER));
        setReasimSteam(tag.getInt(TAG_REASIM_STEAM));
    }

    public void save(CompoundTag tag) {
        tag.putDouble(TAG_HEAT, heat);
        tag.putInt(TAG_REASIM_WATER, reasimWater);
        tag.putInt(TAG_REASIM_STEAM, reasimSteam);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
