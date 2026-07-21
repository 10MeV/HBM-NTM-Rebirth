package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmSubscribableNodeNet;

public class PlasmaNetwork extends HbmSubscribableNodeNet<Object, Object, PlasmaNode> {
    @Override
    protected void pruneStale(long timestamp) {
        // Legacy PlasmaNetwork#update is deliberately empty.
    }
}
