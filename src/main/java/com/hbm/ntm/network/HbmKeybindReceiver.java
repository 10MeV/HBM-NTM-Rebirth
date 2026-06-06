package com.hbm.ntm.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface HbmKeybindReceiver {
    boolean canHandleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind);

    void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed);
}
