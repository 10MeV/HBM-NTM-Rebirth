package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface HbmLegacyControlReceiver extends HbmTileSyncable {
    default boolean hasPermission(ServerPlayer player) {
        return true;
    }

    default void receiveControl(ServerPlayer player, CompoundTag data) {
    }

    default void receiveControl(CompoundTag data) {
    }

    @Override
    default boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return hasPermission(player);
    }

    @Override
    default void handleClientControl(ServerPlayer player, CompoundTag tag) {
        receiveControl(player, tag);
        receiveControl(tag);
    }
}
