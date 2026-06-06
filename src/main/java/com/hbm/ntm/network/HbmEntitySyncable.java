package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public interface HbmEntitySyncable {
    default CompoundTag getClientSyncTag() {
        return new CompoundTag();
    }

    default boolean canSendClientSyncTo(ServerPlayer player) {
        return true;
    }

    default void handleClientSyncTag(CompoundTag tag) {
    }
}
