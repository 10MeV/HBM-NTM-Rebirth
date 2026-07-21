package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.energy.PowerNetMK2;
import com.hbm.ntm.uninos.HbmNetworkProvider;

public enum PowerNetProvider implements HbmNetworkProvider<HbmEnergyNode, HbmPowerNet> {
    THE_PROVIDER;

    @Override
    public HbmPowerNet provideNetwork() {
        return new PowerNetMK2();
    }
}
