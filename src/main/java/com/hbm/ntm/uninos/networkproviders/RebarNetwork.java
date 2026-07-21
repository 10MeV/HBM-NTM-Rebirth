package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmSubscribableNodeNet;

public class RebarNetwork extends HbmSubscribableNodeNet<Object, Object, RebarNode> {
    @Override
    protected void pruneStale(long timestamp) {
        // Legacy RebarNetwork#update is deliberately empty.
    }
}
