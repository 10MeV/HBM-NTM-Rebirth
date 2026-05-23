package com.hbm.ntm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface HbmTileBinaryControlReceiver {
    default boolean canReceiveClientTileBinaryData(ServerPlayer player, ResourceLocation channel, int readableBytes) {
        return true;
    }

    void handleClientTileBinaryData(ServerPlayer player, ResourceLocation channel, FriendlyByteBuf data);
}
