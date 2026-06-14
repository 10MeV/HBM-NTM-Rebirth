package com.hbm.ntm.satellite;

public final class SatelliteMapper extends Satellite {
    public SatelliteMapper() {
        ifaceAcs.add(InterfaceActions.HAS_MAP);
        setSatelliteInterface(Interfaces.SAT_PANEL);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.MAPPER;
    }
}
