package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class RBMKRodFluxState {
    public static final String TAG_FLUX_QUANTITY = "fluxQuantity";
    public static final String TAG_FLUX_MOD = "fluxMod";
    public static final String TAG_FLUX_FAST = "fluxFast";
    public static final String TAG_FLUX_SLOW = "fluxSlow";
    public static final String TAG_HAS_ROD = "hasRod";

    private double fluxFastRatio;
    private double fluxQuantity;
    private double lastFluxQuantity;
    private double lastFluxRatio;
    private boolean hasRod;
    private int rodColor;

    public double fluxFastRatio() {
        return fluxFastRatio;
    }

    public void setFluxFastRatio(double fluxFastRatio) {
        this.fluxFastRatio = clamp01(fluxFastRatio);
    }

    public double fluxQuantity() {
        return fluxQuantity;
    }

    public void setFluxQuantity(double fluxQuantity) {
        this.fluxQuantity = Math.max(0.0D, fluxQuantity);
    }

    public double lastFluxQuantity() {
        return lastFluxQuantity;
    }

    public double lastFluxRatio() {
        return lastFluxRatio;
    }

    public boolean hasRod() {
        return hasRod;
    }

    public void setHasRod(boolean hasRod) {
        this.hasRod = hasRod;
    }

    public int rodColor() {
        return rodColor;
    }

    public void setRodColor(int rodColor) {
        this.rodColor = rodColor;
    }

    public void receiveFlux(double streamFluxQuantity, double streamFluxRatio) {
        double fastFlux = fluxQuantity * fluxFastRatio;
        double fastFluxIn = streamFluxQuantity * streamFluxRatio;
        fluxQuantity += streamFluxQuantity;
        fluxFastRatio = fluxQuantity > 0.0D ? clamp01((fastFlux + fastFluxIn) / fluxQuantity) : 0.0D;
    }

    public void receiveFlux(RBMKNeutronHandler.RBMKNeutronStream stream) {
        receiveFlux(stream.getFluxQuantity(), stream.getFluxRatio());
    }

    public double fluxFromType(RBMKFluxReceiver.NType type) {
        double fastFlux = fluxQuantity * fluxFastRatio;
        double slowFlux = fluxQuantity * (1.0D - fluxFastRatio);
        return switch (type) {
            case SLOW -> slowFlux + fastFlux * 0.5D;
            case FAST -> fastFlux + slowFlux * 0.3D;
            case ANY -> fluxQuantity;
        };
    }

    public void commitActiveRodTick() {
        lastFluxQuantity = fluxQuantity;
        lastFluxRatio = fluxFastRatio;
        fluxQuantity = 0.0D;
        fluxFastRatio = 0.0D;
        hasRod = true;
    }

    public void clearRodTick() {
        lastFluxQuantity = 0.0D;
        lastFluxRatio = 0.0D;
        fluxQuantity = 0.0D;
        fluxFastRatio = 0.0D;
        hasRod = false;
    }

    public void resetFluxAfterRodOverheat() {
        lastFluxQuantity = 0.0D;
        lastFluxRatio = 0.0D;
        fluxQuantity = 0.0D;
        fluxFastRatio = 0.0D;
        hasRod = true;
    }

    public void load(CompoundTag tag) {
        if (tag.contains(TAG_FLUX_FAST) || tag.contains(TAG_FLUX_SLOW)) {
            fluxQuantity = tag.getDouble(TAG_FLUX_FAST) + tag.getDouble(TAG_FLUX_SLOW);
            fluxFastRatio = fluxQuantity > 0.0D ? clamp01(tag.getDouble(TAG_FLUX_FAST) / fluxQuantity) : 0.0D;
        } else {
            fluxQuantity = tag.getDouble(TAG_FLUX_QUANTITY);
            fluxFastRatio = clamp01(tag.getDouble(TAG_FLUX_MOD));
        }
        hasRod = tag.getBoolean(TAG_HAS_ROD);
    }

    public void save(CompoundTag tag) {
        tag.putDouble(TAG_FLUX_QUANTITY, lastFluxQuantity);
        tag.putDouble(TAG_FLUX_MOD, lastFluxRatio);
        tag.putBoolean(TAG_HAS_ROD, hasRod);
    }

    public void saveDiagnostics(CompoundTag tag) {
        tag.putDouble(TAG_FLUX_SLOW, fluxQuantity * (1.0D - fluxFastRatio));
        tag.putDouble(TAG_FLUX_FAST, fluxQuantity * fluxFastRatio);
        tag.putBoolean(TAG_HAS_ROD, hasRod);
    }

    private static double clamp01(double value) {
        if (value < 0.0D || Double.isNaN(value)) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }
}
