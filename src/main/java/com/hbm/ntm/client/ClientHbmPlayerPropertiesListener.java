package com.hbm.ntm.client;

import com.hbm.ntm.player.HbmPlayerProperties;

@FunctionalInterface
public interface ClientHbmPlayerPropertiesListener {
    void onClientHbmPlayerProperties(HbmPlayerProperties.SyncData data);
}
