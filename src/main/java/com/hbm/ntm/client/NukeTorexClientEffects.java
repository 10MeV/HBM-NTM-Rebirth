package com.hbm.ntm.client;

import com.hbm.ntm.client.render.HbmRenderEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class NukeTorexClientEffects {
    private NukeTorexClientEffects() {
    }

    public static void updateFlash(Level level, int age) {
        if (level instanceof ClientLevel clientLevel) {
            clientLevel.setSkyFlashTime(Math.max(clientLevel.getSkyFlashTime(), 4));
        }
        if (age < 10) {
            NukeHudEffects.triggerFlash();
        }
    }

    public static void spawnWarpShockwave(double x, double y, double z, int age) {
        HbmRenderEffects.spawnTorexWarpShockwave(x, y, z, age);
    }

    public static Player localPlayer() {
        return Minecraft.getInstance().player;
    }

    public static boolean applyShockwaveShake(Player player) {
        if (player == null || !NukeHudEffects.triggerShake()) {
            return false;
        }
        player.animateHurt(0.0F);
        player.hurtTime = 15;
        player.hurtDuration = 15;
        return true;
    }
}
