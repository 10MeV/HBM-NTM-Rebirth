package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface HbmItemControlReceiver {
    default boolean canReceiveItemControl(ServerPlayer player, ItemStack stack, CompoundTag tag) {
        return true;
    }

    void handleItemControl(ServerPlayer player, ItemStack stack, CompoundTag tag);
}
