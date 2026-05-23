package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkProvider;

public enum FoundryNetworkProvider implements HbmNetworkProvider<FoundryNode, FoundryNetwork> {
    THE_PROVIDER;

    @Override
    public FoundryNetwork provideNetwork(FoundryNode seedNode) {
        return new FoundryNetwork();
    }
}
