package com.hbm.ntm.network;

import com.hbm.ntm.player.HbmPlayerProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class HbmServerKeybinds {
    public static boolean isPressed(ServerPlayer player, HbmKeybind keybind) {
        return HbmPlayerProperties.getKeyPressed(player, keybind);
    }

    public static void setPressed(ServerPlayer player, HbmKeybind keybind, boolean pressed) {
        HbmPlayerProperties.setKeyPressed(player, keybind, pressed);
    }

    public static void handle(ServerPlayer player, HbmKeybind keybind, boolean pressed) {
        setPressed(player, keybind, pressed);
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && held.getItem() instanceof HbmKeybindReceiver receiver && receiver.canHandleKeybind(player, held, keybind)) {
            receiver.handleKeybind(player, held, keybind, pressed);
        }
    }

    public static void clear(ServerPlayer player) {
        HbmPlayerProperties.clearKeyStates(player);
    }

    private HbmServerKeybinds() {
    }
}
