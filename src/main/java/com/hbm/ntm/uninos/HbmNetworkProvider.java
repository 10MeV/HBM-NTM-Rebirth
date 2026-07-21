package com.hbm.ntm.uninos;

@FunctionalInterface
public interface HbmNetworkProvider<N extends HbmNetworkNode, T extends HbmNodeNet<?, ?, N>> {
    /**
     * Creates one network for this provider type.
     *
     * <p>This keeps the public shape of 1.7.10 {@code INetworkProvider}:
     * the node already carries/selects its provider, so network construction
     * itself has no seed-node argument.</p>
     */
    T provideNetwork();
}
