package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkProvider;

public enum RebarNetworkProvider implements HbmNetworkProvider<RebarNode, RebarNetwork> {
    THE_PROVIDER;

    @Override
    public RebarNetwork provideNetwork(RebarNode seedNode) {
        return new RebarNetwork();
    }
}
