package com.hbm.ntm.neutron;

public record PileNeutronBlockResult(boolean shouldStop, double fluxMultiplier) {
    public static PileNeutronBlockResult pass() {
        return new PileNeutronBlockResult(false, 1.0D);
    }

    public static PileNeutronBlockResult halt() {
        return new PileNeutronBlockResult(true, 0.0D);
    }

    public static PileNeutronBlockResult attenuate(double multiplier) {
        return new PileNeutronBlockResult(false, multiplier);
    }

    public PileNeutronBlockResult {
        fluxMultiplier = Math.max(0.0D, fluxMultiplier);
    }
}
