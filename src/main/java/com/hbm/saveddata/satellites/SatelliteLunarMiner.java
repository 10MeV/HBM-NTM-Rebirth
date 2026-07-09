package com.hbm.saveddata.satellites;

import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.satellite.LegacySatelliteType;

public class SatelliteLunarMiner extends SatelliteMiner {
    static {
        registerCargo(SatelliteLunarMiner.class, HbmItemPoolIds.POOL_SAT_LUNAR);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.LUNAR_MINER;
    }
}
