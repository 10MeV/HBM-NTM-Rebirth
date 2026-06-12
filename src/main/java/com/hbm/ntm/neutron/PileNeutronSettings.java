package com.hbm.ntm.neutron;

public record PileNeutronSettings(
        int range,
        double step,
        PileNeutronBlockRules blockRules,
        PileNeutronRadiationHandler radiationHandler) {
    public static PileNeutronSettings legacyDefaults() {
        return new PileNeutronSettings(
                5,
                0.5D,
                PileLegacyBlockRules.LEGACY_DEFAULTS,
                PileNeutronRadiationHandler.LEGACY_CONTAMINATION);
    }

    public PileNeutronSettings {
        range = Math.max(1, range);
        step = step <= 0.0D ? 0.5D : step;
        blockRules = blockRules == null ? PileNeutronBlockRules.PASS : blockRules;
        radiationHandler = radiationHandler == null ? PileNeutronRadiationHandler.NOOP : radiationHandler;
    }
}
