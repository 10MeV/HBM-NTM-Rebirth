package com.hbm.ntm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface HbmLegacyBufPacketReceiver extends HbmTileBinarySyncProvider, HbmClientTileBinaryReceiver {
    @Override
    default ResourceLocation getClientTileBinarySyncChannel() {
        return HbmNetworkActions.BUF_PACKET;
    }

    @Override
    default void writeClientTileBinaryData(FriendlyByteBuf data) {
        serializeLegacyBufPacket(data);
    }

    @Override
    default void handleClientTileBinaryData(ResourceLocation channel, FriendlyByteBuf data) {
        if (getClientTileBinarySyncChannel().equals(channel)) {
            deserializeLegacyBufPacket(data);
        }
    }

    void serializeLegacyBufPacket(FriendlyByteBuf data);

    void deserializeLegacyBufPacket(FriendlyByteBuf data);
}
