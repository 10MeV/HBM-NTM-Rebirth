package com.hbm.extprop;

import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.player.HbmPlayerProperties;

/**
 * Client-side read facade for migrated HUD/render code. Server authority still
 * lives in HbmPlayerProperties.
 */
public final class ClientHbmPlayerProps {
    public static boolean hasReceivedBook() {
        return ClientHbmPlayerProperties.hasReceivedBook();
    }

    public static float getShield() {
        return ClientHbmPlayerProperties.getShield();
    }

    public static float getMaxShield() {
        return ClientHbmPlayerProperties.getMaxShield();
    }

    public static boolean isBackpackEnabled() {
        return ClientHbmPlayerProperties.isBackpackEnabled();
    }

    public static boolean isMagnetActive() {
        return ClientHbmPlayerProperties.isMagnetActive();
    }

    public static boolean isHudEnabled() {
        return ClientHbmPlayerProperties.isHudEnabled();
    }

    public static boolean shouldRenderHud() {
        return ClientHbmPlayerProperties.shouldRenderHud();
    }

    public static int getReputation() {
        return ClientHbmPlayerProperties.getReputation();
    }

    public static boolean isOnLadder() {
        return ClientHbmPlayerProperties.isOnLadder();
    }

    public static int getDashCount() {
        return ClientHbmPlayerProperties.getDashCount();
    }

    public static int getStamina() {
        return ClientHbmPlayerProperties.getStamina();
    }

    public static int getDashCooldown() {
        return ClientHbmPlayerProperties.getDashCooldown();
    }

    public static HbmPlayerProperties.SyncData snapshot() {
        return ClientHbmPlayerProperties.snapshot();
    }

    private ClientHbmPlayerProps() {
    }
}
