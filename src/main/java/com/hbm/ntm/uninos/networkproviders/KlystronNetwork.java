package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmSubscribableNodeNet;

public class KlystronNetwork extends HbmSubscribableNodeNet<Object, Object, KlystronNode> {
    @Override
    protected void pruneStale(long timestamp) {
        // Legacy KlystronNetwork#update is deliberately empty.
    }
}
