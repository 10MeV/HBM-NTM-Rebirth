package com.hbm.ntm.satellite;

public final class SatelliteScanner extends Satellite {
    public SatelliteScanner() {
        ifaceAcs.add(InterfaceActions.HAS_ORES);
        setSatelliteInterface(Interfaces.SAT_PANEL);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.SCANNER;
    }
}
