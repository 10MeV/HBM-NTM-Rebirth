package com.hbm.ntm.client;

import com.hbm.ntm.player.HbmPlayerProperties;
import net.minecraft.nbt.CompoundTag;

public final class ClientHbmPlayerProperties {
    private static boolean hudEnabled = true;
    private static boolean registered;

    public static boolean isHudEnabled() {
        registerListener();
        return hudEnabled;
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
        hudEnabled = true;
        registered = false;
    }

    private static void apply(CompoundTag data) {
        hudEnabled = !data.contains(HbmPlayerProperties.KEY_ENABLE_HUD)
                || data.getBoolean(HbmPlayerProperties.KEY_ENABLE_HUD);
    }

    private ClientHbmPlayerProperties() {
    }
}
