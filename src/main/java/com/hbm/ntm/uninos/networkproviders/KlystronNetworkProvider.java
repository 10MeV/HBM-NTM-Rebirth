package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmNetworkProvider;

public enum KlystronNetworkProvider implements HbmNetworkProvider<KlystronNode, KlystronNetwork> {
    THE_PROVIDER;

    @Override
    public KlystronNetwork provideNetwork() {
        return new KlystronNetwork();
    }
}
