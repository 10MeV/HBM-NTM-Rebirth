package com.hbm.ntm.client;

import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.client.screen.TrinketInfoScreen;
import net.minecraft.client.Minecraft;

/** Client-only opening point for the legacy bobblehead and snowglobe information panels. */
public final class TrinketInfoScreenBridge {
    private TrinketInfoScreenBridge() {
    }

    public static void open(TrinketVariant.Kind kind, int variant) {
        Minecraft.getInstance().setScreen(new TrinketInfoScreen(kind, variant));
    }
}
