package com.hbm.ntm.neutron;

import com.hbm.ntm.radiation.ChunkRadiationManager;

public record RBMKNeutronSettings(
        double moderatorEfficiency,
        double reflectorEfficiency,
        double absorberEfficiency,
        double absorberHeatConversion,
        int columnHeight,
        int fluxRange,
        int reasimRange,
        RBMKNeutronLeakHandler leakHandler) {
    private static final double DEFAULT_MODERATOR_EFFICIENCY = 1.0D;
    private static final double DEFAULT_REFLECTOR_EFFICIENCY = 1.0D;
    private static final double DEFAULT_ABSORBER_EFFICIENCY = 1.0D;
    private static final double DEFAULT_ABSORBER_HEAT_CONVERSION = 0.05D;
    private static final int DEFAULT_COLUMN_HEIGHT_ABOVE = 3;
    private static final int DEFAULT_FLUX_RANGE = 5;
    private static final int DEFAULT_REASIM_RANGE = 10;

    public static RBMKNeutronSettings legacyDefaults() {
        return new RBMKNeutronSettings(
                DEFAULT_MODERATOR_EFFICIENCY,
                DEFAULT_REFLECTOR_EFFICIENCY,
                DEFAULT_ABSORBER_EFFICIENCY,
                DEFAULT_ABSORBER_HEAT_CONVERSION,
                DEFAULT_COLUMN_HEIGHT_ABOVE + 1,
                DEFAULT_FLUX_RANGE,
                DEFAULT_REASIM_RANGE,
                ChunkRadiationManager::incrementRadiation);
    }

    public RBMKNeutronSettings {
        columnHeight = Math.max(1, columnHeight);
        fluxRange = Math.max(1, fluxRange);
        reasimRange = Math.max(1, reasimRange);
        moderatorEfficiency = clamp01(moderatorEfficiency);
        reflectorEfficiency = clamp01(reflectorEfficiency);
        absorberEfficiency = clamp01(absorberEfficiency);
        absorberHeatConversion = Math.max(0.0D, absorberHeatConversion);
        leakHandler = leakHandler == null ? RBMKNeutronLeakHandler.NOOP : leakHandler;
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
