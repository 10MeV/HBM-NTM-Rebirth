package com.hbm.ntm.client;

import net.minecraft.client.gui.screens.Screen;

public final class ClientTooltipState {
    private ClientTooltipState() {
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }
}
