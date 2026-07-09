package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;

public class SatelliteRadar extends Satellite {
    public SatelliteRadar() {
        this.ifaceAcs.add(InterfaceActions.HAS_MAP);
        this.ifaceAcs.add(InterfaceActions.HAS_RADAR);
        this.satIface = Interfaces.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RADAR;
    }
}
