package com.hbm.ntm.client;

import com.hbm.ntm.client.screen.BobmazonScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public final class BobmazonScreenBridge {
    private BobmazonScreenBridge() {
    }

    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new BobmazonScreen(hand));
    }
}
