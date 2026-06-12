package com.hbm.ntm.client;

import com.hbm.ntm.player.HbmPlayerProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ClientHbmPlayerProperties {
    private static final List<ClientHbmPlayerPropertiesListener> LISTENERS = new ArrayList<>();
    private static boolean hasReceivedBook;
    private static float shield;
    private static float maxShield;
    private static boolean backpackEnabled = true;
    private static boolean magnetEnabled = true;
    private static boolean hudEnabled = true;
    private static int reputation;
    private static boolean onLadder;
    private static int dashCount;
    private static int stamina;
    private static int dashCooldown;
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

    public static boolean isMagnetActive() {
        return isMagnetEnabled();
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

    public static int getDashCount() {
        registerListener();
        return dashCount;
    }

    public static int getStamina() {
        registerListener();
        return stamina;
    }

    public static int getDashCooldown() {
        registerListener();
        return dashCooldown;
    }

    public static HbmPlayerProperties.SyncData snapshot() {
        registerListener();
        return new HbmPlayerProperties.SyncData(hasReceivedBook, shield, maxShield, backpackEnabled, magnetEnabled, hudEnabled,
                reputation, onLadder, dashCount, stamina, dashCooldown);
    }

    public static int syncedEntryCount() {
        return ClientPlayerSyncData.entryCount();
    }

    public static void update(ResourceLocation dataType, CompoundTag data) {
        ClientPlayerSyncData.update(dataType, data);
    }

    public static void update(HbmPlayerProperties.SyncData data) {
        apply(data == null ? HbmPlayerProperties.emptySyncedData() : data);
    }

    public static void addListener(ClientHbmPlayerPropertiesListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
            registerListener();
        }
    }

    public static void removeListener(ClientHbmPlayerPropertiesListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
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
        ClientPlayerSyncData.clearAll();
        hasReceivedBook = false;
        shield = 0.0F;
        maxShield = 0.0F;
        backpackEnabled = true;
        magnetEnabled = true;
        hudEnabled = true;
        reputation = 0;
        onLadder = false;
        dashCount = 0;
        stamina = 0;
        dashCooldown = 0;
        LISTENERS.clear();
        registered = false;
    }

    private static void apply(CompoundTag data) {
        apply(HbmPlayerProperties.readSyncedData(data));
    }

    private static void apply(HbmPlayerProperties.SyncData syncData) {
        hasReceivedBook = syncData.hasReceivedBook();
        shield = syncData.shield();
        maxShield = syncData.maxShield();
        backpackEnabled = syncData.backpackEnabled();
        magnetEnabled = syncData.magnetEnabled();
        hudEnabled = syncData.hudEnabled();
        reputation = syncData.reputation();
        onLadder = syncData.onLadder();
        dashCount = syncData.dashCount();
        stamina = syncData.stamina();
        dashCooldown = syncData.dashCooldown();
        for (ClientHbmPlayerPropertiesListener listener : List.copyOf(LISTENERS)) {
            listener.onClientHbmPlayerProperties(syncData);
        }
    }

    private ClientHbmPlayerProperties() {
    }
}
