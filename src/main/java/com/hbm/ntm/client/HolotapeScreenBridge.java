package com.hbm.ntm.client;

import com.hbm.ntm.client.screen.HolotapeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

/** Client-only opening point for the legacy holotape text browser. */
public final class HolotapeScreenBridge {
    private HolotapeScreenBridge() {
    }

    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new HolotapeScreen(hand));
    }
}
