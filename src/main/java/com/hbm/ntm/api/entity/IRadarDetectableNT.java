package com.hbm.ntm.api.entity;

/**
 * Legacy-name bridge for the newer 1.7.10 radar detection API.
 */
@Deprecated(forRemoval = false)
public interface IRadarDetectableNT extends RadarDetectable {
    String getUnlocalizedName();

    boolean canBeSeenBy(Object radar);

    boolean paramsApplicable(RadarScanParams params);

    boolean suppliesRedstone(RadarScanParams params);

    @Override
    default String getRadarName() {
        return getUnlocalizedName();
    }

    @Override
    default boolean canBeSeenBy(RadarContext radar) {
        return canBeSeenBy((Object) radar);
    }

    @Override
    default boolean paramsApplicable(RadarDetectable.RadarScanParams params) {
        return paramsApplicable(RadarScanParams.fromModern(params));
    }

    @Override
    default boolean suppliesRedstone(RadarDetectable.RadarScanParams params) {
        return suppliesRedstone(RadarScanParams.fromModern(params));
    }

    class RadarScanParams {
        public boolean scanMissiles = true;
        public boolean scanShells = true;
        public boolean scanPlayers = true;
        public boolean smartMode = true;

        public RadarScanParams(boolean scanMissiles, boolean scanShells, boolean scanPlayers, boolean smartMode) {
            this.scanMissiles = scanMissiles;
            this.scanShells = scanShells;
            this.scanPlayers = scanPlayers;
            this.smartMode = smartMode;
        }

        private static RadarScanParams fromModern(RadarDetectable.RadarScanParams params) {
            if (params == null) {
                return new RadarScanParams(true, true, true, true);
            }
            return new RadarScanParams(params.scanMissiles(), params.scanShells(), params.scanPlayers(), params.smartMode());
        }
    }
}
