package com.hbm.ntm.network;

import net.minecraft.server.level.ServerPlayer;

public interface HbmLegacyButtonReceiver {
    default boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return true;
    }

    void handleLegacyButton(ServerPlayer player, int value, int id);
}
