package com.hbm.ntm.api.item;

public enum HazardClass {
    GAS_LUNG("hazard.gasChlorine"),
    GAS_MONOXIDE("hazard.gasMonoxide"),
    GAS_INERT("hazard.gasInert"),
    PARTICLE_COARSE("hazard.particleCoarse"),
    PARTICLE_FINE("hazard.particleFine"),
    BACTERIA("hazard.bacteria"),
    GAS_BLISTERING("hazard.corrosive"),
    SAND("hazard.sand"),
    LIGHT("hazard.light");

    private final String translationKey;

    HazardClass(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
