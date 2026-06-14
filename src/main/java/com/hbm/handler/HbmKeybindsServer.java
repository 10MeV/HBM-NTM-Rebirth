package com.hbm.handler;

import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side legacy keybind facade. Item dispatch is delegated to the modern
 * keybind runtime.
 */
public final class HbmKeybindsServer {
    public static void onPressedServer(ServerPlayer player, EnumKeybind key, boolean state) {
        if (player == null || key == null) {
            return;
        }
        HbmServerKeybinds.handle(player, HbmKeybinds.toModern(key), state);
    }

    private HbmKeybindsServer() {
    }
}
