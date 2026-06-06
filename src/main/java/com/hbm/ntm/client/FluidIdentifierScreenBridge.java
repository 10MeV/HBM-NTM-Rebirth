package com.hbm.ntm.client;

import com.hbm.ntm.client.screen.FluidIdentifierScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public final class FluidIdentifierScreenBridge {
    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new FluidIdentifierScreen(hand));
    }

    private FluidIdentifierScreenBridge() {
    }
}
