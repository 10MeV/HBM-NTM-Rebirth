package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;

public class SatelliteScanner extends Satellite {
    public SatelliteScanner() {
        this.ifaceAcs.add(InterfaceActions.HAS_ORES);
        this.satIface = Interfaces.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.SCANNER;
    }
}
