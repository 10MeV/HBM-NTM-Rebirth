package com.hbm.ntm.satellite;

public final class SatelliteLunarMiner extends SatelliteMiner {
    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.LUNAR_MINER;
    }
}
