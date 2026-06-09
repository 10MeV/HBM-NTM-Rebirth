package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface HbmLegacyItemControlReceiver extends HbmItemControlReceiver {
    void receiveControl(ItemStack stack, CompoundTag data);

    @Override
    default void handleItemControl(ServerPlayer player, ItemStack stack, CompoundTag tag) {
        receiveControl(stack, tag);
    }
}
