package com.hbm.ntm.client;

import com.hbm.ntm.client.ClientRadiationData.PlayerRadiationSyncData;

@FunctionalInterface
public interface ClientRadiationDataListener {
    void onClientRadiationData(PlayerRadiationSyncData data);
}
