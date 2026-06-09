package com.hbm.ntm.api.entity;

public interface RadarDetectable {
    int TIER0 = 0;
    int TIER1 = 1;
    int TIER2 = 2;
    int TIER3 = 3;
    int TIER4 = 4;
    int TIER10 = 5;
    int TIER10_15 = 6;
    int TIER15 = 7;
    int TIER15_20 = 8;
    int TIER20 = 9;
    int TIER_AB = 10;
    int PLAYER = 11;
    int ARTY = 12;
    int SPECIAL = 13;

    String TARGET_TIER0 = "radar.target.tier0";
    String TARGET_TIER1 = "radar.target.tier1";
    String TARGET_TIER2 = "radar.target.tier2";
    String TARGET_TIER3 = "radar.target.tier3";
    String TARGET_TIER4 = "radar.target.tier4";
    String TARGET_CUSTOM_10 = "radar.target.custom10";
    String TARGET_CUSTOM_10_15 = "radar.target.custom1015";
    String TARGET_CUSTOM_15 = "radar.target.custom15";
    String TARGET_CUSTOM_15_20 = "radar.target.custom1520";
    String TARGET_CUSTOM_20 = "radar.target.custom20";
    String TARGET_ABM = "radar.target.abm";
    String TARGET_DOOMSDAY = "radar.target.doomsday";
    String TARGET_SHUTTLE = "radar.target.shuttle";

    String getRadarName();

    int getBlipLevel();

    boolean canBeSeenBy(RadarContext radar);

    boolean paramsApplicable(RadarScanParams params);

    boolean suppliesRedstone(RadarScanParams params);

    record RadarScanParams(boolean scanMissiles, boolean scanShells, boolean scanPlayers, boolean smartMode) {
        public static final RadarScanParams DEFAULT = new RadarScanParams(true, true, true, true);
    }
}
