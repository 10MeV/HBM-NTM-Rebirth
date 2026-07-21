package com.hbm.ntm.client.overlay;

import com.hbm.ntm.radiation.ArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.RandomSource;

/**
 * Client-only state from 1.7.10 {@code BlockAshes.ashes}.  The old fullscreen
 * render hooks were disabled in source, so this class deliberately owns state
 * and decay only; it does not introduce a new HUD effect.
 */
public final class LegacyAshExposureOverlay {
    private static int ashes;
    private static Player lastLocalPlayer;

    public static void accumulate(RandomSource random) {
        if (random.nextInt(25) != 0) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null || ashes >= ArmorUtil.getAshExposureLimit(player)) {
            return;
        }
        ashes++;
    }

    /** Mirrors the old client START-tick clamp and two-point decay. */
    public static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (minecraft.level == null || player == null) {
            return;
        }
        if (player != lastLocalPlayer) {
            ashes = 0;
            lastLocalPlayer = player;
        }
        if (ashes > 256) {
            ashes = 256;
        }
        if (ashes > 0) {
            ashes -= 2;
        }
        if (ashes < 0) {
            ashes = 0;
        }
    }

    private LegacyAshExposureOverlay() {
    }
}
