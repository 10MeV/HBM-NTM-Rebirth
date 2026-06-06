package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface HbmMenuActionReceiver {
    default boolean canReceiveMenuAction(ServerPlayer player, int action, int value, CompoundTag data) {
        return true;
    }

    void handleMenuAction(ServerPlayer player, int action, int value, CompoundTag data);
}
