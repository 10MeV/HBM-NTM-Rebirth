package com.hbm.ntm.network;

import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class HbmPermaSyncData {
    public static final String TAG_DEATH_EFFECT_ENTITY_IDS = "deathEffectEntityIds";

    public static CompoundTag writeForPlayer(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        appendForPlayer(tag, player);
        return tag;
    }

    public static void appendForPlayer(CompoundTag tag, ServerPlayer player) {
        if (tag == null || player == null) {
            return;
        }
        PollutionSavedData.appendPermaSyncData(tag,
                PollutionManager.getPollutionData(player.level(), player.blockPosition()));
        TomImpactSavedData.appendPermaSyncData(player.serverLevel(), tag);
        appendDeathEffectEntityIds(tag, deathEffectEntityIds(player));
    }

    public static void appendDeathEffectEntityIds(CompoundTag tag, int[] entityIds) {
        if (tag != null) {
            tag.putIntArray(TAG_DEATH_EFFECT_ENTITY_IDS, entityIds == null ? new int[0] : entityIds.clone());
        }
    }

    public static int[] deathEffectEntityIds(ServerPlayer player) {
        return new int[0];
    }

    private HbmPermaSyncData() {
    }
}
