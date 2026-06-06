package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface HbmTypedMenuActionReceiver {
    default boolean canReceiveTypedMenuAction(ServerPlayer player, ResourceLocation actionType,
                                             int value, CompoundTag data) {
        return true;
    }

    void handleTypedMenuAction(ServerPlayer player, ResourceLocation actionType, int value, CompoundTag data);
}
