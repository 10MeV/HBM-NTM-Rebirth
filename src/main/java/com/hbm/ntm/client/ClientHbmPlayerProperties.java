package com.hbm.ntm.client;

import com.hbm.ntm.player.HbmPlayerProperties;
import net.minecraft.nbt.CompoundTag;

public final class ClientHbmPlayerProperties {
    private static boolean hasReceivedBook;
    private static float shield;
    private static float maxShield;
    private static boolean backpackEnabled = true;
    private static boolean magnetEnabled = true;
    private static boolean hudEnabled = true;
    private static int reputation;
    private static boolean onLadder;
    private static boolean registered;

    public static boolean hasReceivedBook() {
        registerListener();
        return hasReceivedBook;
    }

    public static float getShield() {
        registerListener();
        return shield;
    }

    public static float getMaxShield() {
        registerListener();
        return maxShield;
    }

    public static boolean isBackpackEnabled() {
        registerListener();
        return backpackEnabled;
    }

    public static boolean isMagnetEnabled() {
        registerListener();
        return magnetEnabled;
    }

    public static boolean isHudEnabled() {
        registerListener();
        return hudEnabled;
    }

    public static int getReputation() {
        registerListener();
        return reputation;
    }

    public static boolean isOnLadder() {
        registerListener();
        return onLadder;
    }

    public static boolean shouldRenderHud() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        return minecraft.player != null && !minecraft.options.hideGui && isHudEnabled();
    }

    public static void registerListener() {
        if (registered) {
            return;
        }
        ClientPlayerSyncData.addListener((dataType, data) -> {
            if (HbmPlayerProperties.DATA_TYPE.equals(dataType)) {
                apply(data);
            }
        });
        ClientPlayerSyncData.get(HbmPlayerProperties.DATA_TYPE).ifPresent(ClientHbmPlayerProperties::apply);
        registered = true;
    }

    public static void clearAll() {
        hasReceivedBook = false;
        shield = 0.0F;
        maxShield = 0.0F;
        backpackEnabled = true;
        magnetEnabled = true;
        hudEnabled = true;
        reputation = 0;
        onLadder = false;
        registered = false;
    }

    private static void apply(CompoundTag data) {
        hasReceivedBook = data.getBoolean(HbmPlayerProperties.KEY_HAS_RECEIVED_BOOK);
        shield = data.getFloat(HbmPlayerProperties.KEY_SHIELD);
        maxShield = data.getFloat(HbmPlayerProperties.KEY_MAX_SHIELD);
        backpackEnabled = !data.contains(HbmPlayerProperties.KEY_ENABLE_BACKPACK)
                || data.getBoolean(HbmPlayerProperties.KEY_ENABLE_BACKPACK);
        magnetEnabled = !data.contains(HbmPlayerProperties.KEY_ENABLE_MAGNET)
                || data.getBoolean(HbmPlayerProperties.KEY_ENABLE_MAGNET);
        hudEnabled = !data.contains(HbmPlayerProperties.KEY_ENABLE_HUD)
                || data.getBoolean(HbmPlayerProperties.KEY_ENABLE_HUD);
        reputation = data.getInt(HbmPlayerProperties.KEY_REPUTATION);
        onLadder = data.getBoolean(HbmPlayerProperties.KEY_IS_ON_LADDER);
    }

    private ClientHbmPlayerProperties() {
    }
}
