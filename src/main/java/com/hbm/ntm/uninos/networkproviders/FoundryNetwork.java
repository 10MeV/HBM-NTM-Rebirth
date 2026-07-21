package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.uninos.HbmSubscribableNodeNet;

public class FoundryNetwork extends HbmSubscribableNodeNet<Object, Object, FoundryNode> {
    @Override
    protected void pruneStale(long timestamp) {
        // Legacy FoundryNetwork#update is deliberately empty.
    }
}
