package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface HbmTypedTileActionReceiver {
    default boolean canReceiveTypedTileAction(ServerPlayer player, ResourceLocation actionType,
                                              int value, CompoundTag data) {
        return true;
    }

    void handleTypedTileAction(ServerPlayer player, ResourceLocation actionType, int value, CompoundTag data);
}
