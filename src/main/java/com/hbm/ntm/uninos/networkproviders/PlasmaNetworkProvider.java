package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkProvider;

public enum PlasmaNetworkProvider implements HbmNetworkProvider<PlasmaNode, PlasmaNetwork> {
    THE_PROVIDER;

    @Override
    public PlasmaNetwork provideNetwork(PlasmaNode seedNode) {
        return new PlasmaNetwork();
    }
}
