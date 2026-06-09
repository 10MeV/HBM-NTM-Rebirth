package com.hbm.ntm.api.entity;

public record LegacyMissileRadarProfile(String radarName, int blipLevel, boolean visible, boolean redstoneEligible) {
    public static final LegacyMissileRadarProfile UNKNOWN =
            new LegacyMissileRadarProfile("Unknown", RadarDetectable.SPECIAL, true, true);
    public static final LegacyMissileRadarProfile TIER0 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_TIER0, RadarDetectable.TIER0, true, true);
    public static final LegacyMissileRadarProfile TIER1 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_TIER1, RadarDetectable.TIER1, true, true);
    public static final LegacyMissileRadarProfile TIER2 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_TIER2, RadarDetectable.TIER2, true, true);
    public static final LegacyMissileRadarProfile TIER3 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_TIER3, RadarDetectable.TIER3, true, true);
    public static final LegacyMissileRadarProfile TIER4 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_TIER4, RadarDetectable.TIER4, true, true);
    public static final LegacyMissileRadarProfile CUSTOM_10 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_CUSTOM_10, RadarDetectable.TIER10, true, true);
    public static final LegacyMissileRadarProfile CUSTOM_10_15 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_CUSTOM_10_15,
                    RadarDetectable.TIER10_15, true, true);
    public static final LegacyMissileRadarProfile CUSTOM_15 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_CUSTOM_15, RadarDetectable.TIER15, true, true);
    public static final LegacyMissileRadarProfile CUSTOM_15_20 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_CUSTOM_15_20,
                    RadarDetectable.TIER15_20, true, true);
    public static final LegacyMissileRadarProfile CUSTOM_20 =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_CUSTOM_20, RadarDetectable.TIER20, true, true);
    public static final LegacyMissileRadarProfile ANTI_BALLISTIC =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_ABM, RadarDetectable.TIER_AB, true, false);
    public static final LegacyMissileRadarProfile SHUTTLE =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_SHUTTLE, RadarDetectable.TIER3, true, true);
    public static final LegacyMissileRadarProfile STEALTH =
            new LegacyMissileRadarProfile(RadarDetectable.TARGET_TIER1, RadarDetectable.TIER1, false, true);

    public static LegacyMissileRadarProfile standardTier(int tier) {
        return switch (tier) {
            case RadarDetectable.TIER0 -> TIER0;
            case RadarDetectable.TIER1 -> TIER1;
            case RadarDetectable.TIER2 -> TIER2;
            case RadarDetectable.TIER3 -> TIER3;
            case RadarDetectable.TIER4 -> TIER4;
            default -> UNKNOWN;
        };
    }

    public boolean canBeSeenBy(RadarContext radar) {
        return visible;
    }

    public boolean paramsApplicable(RadarDetectable.RadarScanParams params) {
        return params.scanMissiles();
    }

    public boolean suppliesRedstone(RadarDetectable.RadarScanParams params, double verticalMotion) {
        return redstoneEligible && (!params.smartMode() || verticalMotion < 0.0D);
    }
}
