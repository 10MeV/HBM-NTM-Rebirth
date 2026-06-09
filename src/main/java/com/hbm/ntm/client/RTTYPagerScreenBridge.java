package com.hbm.ntm.client;

import com.hbm.ntm.client.screen.RTTYPagerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public final class RTTYPagerScreenBridge {
    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new RTTYPagerScreen(hand));
    }

    private RTTYPagerScreenBridge() {
    }
}
