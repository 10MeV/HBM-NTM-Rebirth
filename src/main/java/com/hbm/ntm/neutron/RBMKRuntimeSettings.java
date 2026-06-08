package com.hbm.ntm.neutron;

public record RBMKRuntimeSettings(
        double passiveCooling,
        double passiveCoolingInner,
        double columnHeatFlow,
        double fuelDiffusionModifier,
        double fuelHeatProvision,
        double boilerHeatConsumption,
        double controlSpeedModifier,
        double reactivityModifier,
        double outgasserSpeedModifier,
        boolean reasimBoilers,
        double reasimBoilerSpeed,
        boolean depletionEnabled,
        boolean xenonEnabled,
        boolean meltdownsDisabled) {
    public static RBMKRuntimeSettings legacyDefaults() {
        return new RBMKRuntimeSettings(
                2.5D,
                0.1D,
                0.2D,
                1.0D,
                0.2D,
                0.1D,
                1.0D,
                1.0D,
                1.0D,
                false,
                0.05D,
                true,
                true,
                false);
    }

    public RBMKRuntimeSettings {
        passiveCooling = Math.max(0.0D, passiveCooling);
        passiveCoolingInner = Math.max(0.0D, passiveCoolingInner);
        columnHeatFlow = clamp01(columnHeatFlow);
        fuelDiffusionModifier = Math.max(0.0D, fuelDiffusionModifier);
        fuelHeatProvision = clamp01(fuelHeatProvision);
        boilerHeatConsumption = Math.max(0.0D, boilerHeatConsumption);
        controlSpeedModifier = Math.max(0.0D, controlSpeedModifier);
        reactivityModifier = Math.max(0.0D, reactivityModifier);
        outgasserSpeedModifier = Math.max(0.0D, outgasserSpeedModifier);
        reasimBoilerSpeed = clamp01(reasimBoilerSpeed);
    }

    private static double clamp01(double value) {
        if (value < 0.0D) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }
}
