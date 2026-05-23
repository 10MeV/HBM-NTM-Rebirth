package com.hbm.ntm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface HbmClientTileBinaryReceiver {
    void handleClientTileBinaryData(ResourceLocation channel, FriendlyByteBuf data);
}
