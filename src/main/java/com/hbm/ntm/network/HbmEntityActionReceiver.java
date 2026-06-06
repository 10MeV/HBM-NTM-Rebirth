package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface HbmEntityActionReceiver {
    default boolean canReceiveEntityAction(ServerPlayer player, ResourceLocation actionType, CompoundTag data) {
        return true;
    }

    void handleEntityAction(ServerPlayer player, ResourceLocation actionType, CompoundTag data);
}
