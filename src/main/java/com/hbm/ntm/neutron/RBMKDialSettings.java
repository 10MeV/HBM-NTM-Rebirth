package com.hbm.ntm.neutron;

import com.hbm.ntm.radiation.ChunkRadiationManager;

public record RBMKDialSettings(
        double passiveCooling,
        double passiveCoolingInner,
        double columnHeatFlow,
        double fuelDiffusionModifier,
        double fuelHeatProvision,
        int columnHeightAbove,
        boolean permanentScrap,
        double boilerHeatConsumption,
        double controlSpeedModifier,
        double reactivityModifier,
        double outgasserSpeedModifier,
        double surgeModifier,
        int fluxRange,
        int reasimRange,
        boolean reasimBoilers,
        double reasimBoilerSpeed,
        boolean meltdownsDisabled,
        boolean meltdownOverpressure,
        double moderatorEfficiency,
        double absorberEfficiency,
        double reflectorEfficiency,
        boolean depletionEnabled,
        boolean xenonEnabled,
        double absorberHeatConversion) {
    public static RBMKDialSettings legacyDefaults() {
        return new RBMKDialSettings(
                2.5D,
                0.1D,
                0.2D,
                1.0D,
                0.2D,
                4,
                true,
                0.1D,
                1.0D,
                1.0D,
                1.0D,
                1.0D,
                5,
                10,
                false,
                0.05D,
                false,
                false,
                1.0D,
                1.0D,
                1.0D,
                true,
                true,
                0.05D);
    }

    public RBMKDialSettings {
        passiveCooling = Math.max(0.0D, passiveCooling);
        passiveCoolingInner = Math.max(0.0D, passiveCoolingInner);
        columnHeatFlow = clamp01(columnHeatFlow);
        fuelDiffusionModifier = Math.max(0.0D, fuelDiffusionModifier);
        fuelHeatProvision = clamp01(fuelHeatProvision);
        columnHeightAbove = Math.max(1, columnHeightAbove);
        boilerHeatConsumption = Math.max(0.0D, boilerHeatConsumption);
        controlSpeedModifier = Math.max(0.0D, controlSpeedModifier);
        reactivityModifier = Math.max(0.0D, reactivityModifier);
        outgasserSpeedModifier = Math.max(0.0D, outgasserSpeedModifier);
        surgeModifier = Math.max(0.0D, surgeModifier);
        fluxRange = Math.max(1, fluxRange);
        reasimRange = Math.max(1, reasimRange);
        reasimBoilerSpeed = clamp01(reasimBoilerSpeed);
        moderatorEfficiency = clamp01(moderatorEfficiency);
        absorberEfficiency = clamp01(absorberEfficiency);
        reflectorEfficiency = clamp01(reflectorEfficiency);
        absorberHeatConversion = clamp01(absorberHeatConversion);
    }

    public RBMKNeutronSettings toNeutronSettings() {
        return toNeutronSettings(ChunkRadiationManager::incrementRadiation);
    }

    public RBMKNeutronSettings toNeutronSettings(RBMKNeutronLeakHandler leakHandler) {
        return new RBMKNeutronSettings(
                moderatorEfficiency,
                reflectorEfficiency,
                absorberEfficiency,
                absorberHeatConversion,
                columnHeightAbove + 1,
                fluxRange,
                reasimRange,
                leakHandler);
    }

    public RBMKRuntimeSettings toRuntimeSettings() {
        return new RBMKRuntimeSettings(
                passiveCooling,
                passiveCoolingInner,
                columnHeatFlow,
                fuelDiffusionModifier,
                fuelHeatProvision,
                boilerHeatConsumption,
                controlSpeedModifier,
                reactivityModifier,
                outgasserSpeedModifier,
                reasimBoilers,
                reasimBoilerSpeed,
                depletionEnabled,
                xenonEnabled,
                meltdownsDisabled,
                permanentScrap,
                meltdownOverpressure);
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
