package com.hbm.ntm.client;

@FunctionalInterface
public interface ClientHbmLivingPropertiesListener {
    void onClientHbmLivingProperties(ClientHbmLivingProperties.ClientLivingSyncData data);
}
