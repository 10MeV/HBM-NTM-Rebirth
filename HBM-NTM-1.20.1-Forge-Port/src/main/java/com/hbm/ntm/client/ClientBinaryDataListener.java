package com.hbm.ntm.client;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface ClientBinaryDataListener {
    void onClientBinaryData(ResourceLocation channel, String name, byte[] payload, boolean cleared, int readyVersion);
}
