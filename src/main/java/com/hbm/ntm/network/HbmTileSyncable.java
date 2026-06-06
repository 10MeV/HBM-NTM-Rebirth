package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface HbmTileSyncable {
    default CompoundTag getClientSyncTag() {
        return new CompoundTag();
    }

    default boolean canSendClientSyncTo(ServerPlayer player) {
        return true;
    }

    default void handleClientSyncTag(CompoundTag tag) {
    }

    default boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return true;
    }

    default void handleClientControl(ServerPlayer player, CompoundTag tag) {
    }
}
