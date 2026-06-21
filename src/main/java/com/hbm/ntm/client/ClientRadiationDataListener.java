package com.hbm.ntm.client;

@FunctionalInterface
public interface ClientRadiationDataListener {
    void onClientRadiationData(ClientRadiationData.PlayerRadiationSyncData data);
}
