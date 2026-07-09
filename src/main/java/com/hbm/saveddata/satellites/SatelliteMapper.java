package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;

public class SatelliteMapper extends Satellite {
    public SatelliteMapper() {
        this.ifaceAcs.add(InterfaceActions.HAS_MAP);
        this.satIface = Interfaces.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.MAPPER;
    }
}
