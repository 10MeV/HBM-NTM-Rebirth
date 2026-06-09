package com.hbm.ntm.client;

@FunctionalInterface
interface ClientRadiationDataListener {
    void onClientRadiationData(ClientRadiationData.PlayerRadiationSyncData data);
}
