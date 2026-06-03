package com.hbm.ntm.player;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class HbmPlayerProperties {
    public static final ResourceLocation DATA_TYPE = new ResourceLocation(HbmNtm.MOD_ID, "player_props");
    public static final int NOTICE_ID_HUD = 7;
    public static final String TAG_ROOT = "hbm_player_props";
    public static final String KEY_ENABLE_HUD = "enableHUD";
    private static final int NOTICE_MILLIS = 1_000;

    public static boolean isHudEnabled(Player player) {
        if (player == null) {
            return true;
        }
        CompoundTag root = player.getPersistentData().getCompound(TAG_ROOT);
        return !root.contains(KEY_ENABLE_HUD) || root.getBoolean(KEY_ENABLE_HUD);
    }

    public static void setHudEnabled(ServerPlayer player, boolean enabled) {
        CompoundTag root = player.getPersistentData().getCompound(TAG_ROOT);
        root.putBoolean(KEY_ENABLE_HUD, enabled);
        player.getPersistentData().put(TAG_ROOT, root);
        sync(player);
    }

    public static void toggleHud(ServerPlayer player) {
        boolean enabled = !isHudEnabled(player);
        setHudEnabled(player, enabled);
        ModMessages.informPlayer(player, Component.literal(enabled ? "HUD ON" : "HUD OFF"), NOTICE_ID_HUD, NOTICE_MILLIS);
    }

    public static void sync(ServerPlayer player) {
        CompoundTag data = new CompoundTag();
        data.putBoolean(KEY_ENABLE_HUD, isHudEnabled(player));
        ModMessages.syncPlayerProperties(player, DATA_TYPE, data);
    }

    public static void copyForRespawn(Player original, Player replacement) {
        if (original.getPersistentData().contains(TAG_ROOT)) {
            replacement.getPersistentData().put(TAG_ROOT, original.getPersistentData().getCompound(TAG_ROOT).copy());
        }
    }

    private HbmPlayerProperties() {
    }
}
