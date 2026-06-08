package com.hbm.ntm.client;

import com.hbm.ntm.client.ClientPollutionData.PlayerPollutionSyncData;

@FunctionalInterface
public interface ClientPollutionDataListener {
    void onClientPollutionData(PlayerPollutionSyncData data);
}
