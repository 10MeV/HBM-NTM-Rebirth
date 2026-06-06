package com.hbm.ntm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface HbmTileBinarySyncProvider {
    default ResourceLocation getClientTileBinarySyncChannel() {
        return HbmNetworkActions.BUF_PACKET;
    }

    default boolean canSendClientTileBinaryDataTo(ServerPlayer player, ResourceLocation channel) {
        return getClientTileBinarySyncChannel().equals(channel);
    }

    void writeClientTileBinaryData(FriendlyByteBuf data);
}
