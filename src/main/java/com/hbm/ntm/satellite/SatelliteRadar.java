package com.hbm.ntm.satellite;

public final class SatelliteRadar extends Satellite {
    public SatelliteRadar() {
        ifaceAcs.add(InterfaceActions.HAS_MAP);
        ifaceAcs.add(InterfaceActions.HAS_RADAR);
        setSatelliteInterface(Interfaces.SAT_PANEL);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RADAR;
    }
}
