package com.hbm.ntm.api.entity;

public interface LegacyMissileRadarDetectable extends RadarDetectable {
    LegacyMissileRadarProfile radarProfile();

    double radarVerticalMotion();

    @Override
    default String getRadarName() {
        return radarProfile().radarName();
    }

    @Override
    default int getBlipLevel() {
        return radarProfile().blipLevel();
    }

    @Override
    default boolean canBeSeenBy(RadarContext radar) {
        return radarProfile().canBeSeenBy(radar);
    }

    @Override
    default boolean paramsApplicable(RadarScanParams params) {
        return radarProfile().paramsApplicable(params);
    }

    @Override
    default boolean suppliesRedstone(RadarScanParams params) {
        return radarProfile().suppliesRedstone(params, radarVerticalMotion());
    }
}
