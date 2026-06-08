package com.hbm.ntm.client;

import com.hbm.ntm.client.screen.SatelliteCoordScreen;
import com.hbm.ntm.client.screen.SatellitePanelScreen;
import com.hbm.ntm.satellite.SatelliteInterfaceItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public final class SatelliteScreenBridge {
    public static void open(InteractionHand hand, SatelliteInterfaceItem.Mode mode) {
        InteractionHand safeHand = hand == null ? InteractionHand.MAIN_HAND : hand;
        Minecraft minecraft = Minecraft.getInstance();
        if (mode == SatelliteInterfaceItem.Mode.COORD) {
            minecraft.setScreen(new SatelliteCoordScreen(safeHand));
        } else {
            minecraft.setScreen(new SatellitePanelScreen(safeHand));
        }
    }

    private SatelliteScreenBridge() {
    }
}
