package com.hbm.ntm.uninos.networkproviders.pneumatic;

import com.hbm.ntm.uninos.HbmNetworkProvider;

public enum PneumaticNetworkProvider implements HbmNetworkProvider<PneumaticNode, PneumaticNetwork> {
    THE_PROVIDER;

    @Override
    public PneumaticNetwork provideNetwork(PneumaticNode seedNode) {
        return new PneumaticNetwork();
    }
}
