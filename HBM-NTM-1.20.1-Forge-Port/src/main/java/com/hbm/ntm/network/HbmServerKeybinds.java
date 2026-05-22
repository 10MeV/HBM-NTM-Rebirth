package com.hbm.ntm.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HbmServerKeybinds {
    private static final Map<UUID, EnumSet<HbmKeybind>> PRESSED = new ConcurrentHashMap<>();

    public static boolean isPressed(ServerPlayer player, HbmKeybind keybind) {
        EnumSet<HbmKeybind> keys = PRESSED.get(player.getUUID());
        return keys != null && keys.contains(keybind);
    }

    public static void setPressed(ServerPlayer player, HbmKeybind keybind, boolean pressed) {
        EnumSet<HbmKeybind> keys = PRESSED.computeIfAbsent(player.getUUID(), ignored -> EnumSet.noneOf(HbmKeybind.class));
        if (pressed) {
            keys.add(keybind);
        } else {
            keys.remove(keybind);
            if (keys.isEmpty()) {
                PRESSED.remove(player.getUUID());
            }
        }
    }

    public static void handle(ServerPlayer player, HbmKeybind keybind, boolean pressed) {
        setPressed(player, keybind, pressed);
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && held.getItem() instanceof HbmKeybindReceiver receiver && receiver.canHandleKeybind(player, held, keybind)) {
            receiver.handleKeybind(player, held, keybind, pressed);
        }
    }

    public static void clear(ServerPlayer player) {
        PRESSED.remove(player.getUUID());
    }

    private HbmServerKeybinds() {
    }
}
