package com.hbm.ntm.client;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ClientInformMessageListener {
    void onClientInformMessage(Component message, int id, int millis);
}
