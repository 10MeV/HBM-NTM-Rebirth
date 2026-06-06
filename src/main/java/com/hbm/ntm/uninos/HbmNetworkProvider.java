package com.hbm.ntm.uninos;

@FunctionalInterface
public interface HbmNetworkProvider<N extends HbmNetworkNode, T extends HbmNodeNet<N>> {
    T provideNetwork(N seedNode);
}
