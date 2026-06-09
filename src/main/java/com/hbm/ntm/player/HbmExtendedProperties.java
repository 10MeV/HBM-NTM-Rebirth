package com.hbm.ntm.player;

import com.hbm.ntm.network.ModMessages;
import net.minecraft.server.level.ServerPlayer;

public final class HbmExtendedProperties {
    public static SyncData writeSyncedData(ServerPlayer player, float chunkRadiation, float resistance) {
        return new SyncData(
                HbmLivingProperties.writeSyncedData(player, chunkRadiation, resistance),
                HbmPlayerProperties.writePlayerSyncData(player));
    }

    public static SyncData emptySyncedData() {
        return new SyncData(HbmLivingProperties.emptySyncedData(), HbmPlayerProperties.emptySyncedData());
    }

    public static void sync(ServerPlayer player, float chunkRadiation, float resistance) {
        ModMessages.syncExtendedProperties(player, chunkRadiation, resistance);
    }

    public static void syncThreaded(ServerPlayer player, float chunkRadiation, float resistance) {
        ModMessages.syncExtendedPropertiesThreaded(player, chunkRadiation, resistance);
    }

    public record SyncData(HbmLivingProperties.SyncData living, HbmPlayerProperties.SyncData player) {
        public SyncData {
            living = living == null ? HbmLivingProperties.emptySyncedData() : living;
            player = player == null ? HbmPlayerProperties.emptySyncedData() : player;
        }
    }

    private HbmExtendedProperties() {
    }
}
