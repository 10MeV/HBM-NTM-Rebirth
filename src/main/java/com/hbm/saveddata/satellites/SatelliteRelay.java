package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;

public class SatelliteRelay extends Satellite {
    public SatelliteRelay() {
        this.satIface = Interfaces.NONE;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RELAY;
    }
}
