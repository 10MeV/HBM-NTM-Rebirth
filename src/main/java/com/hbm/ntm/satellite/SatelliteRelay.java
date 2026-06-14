package com.hbm.ntm.satellite;

public final class SatelliteRelay extends Satellite {
    public SatelliteRelay() {
        setSatelliteInterface(Interfaces.NONE);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RELAY;
    }
}
