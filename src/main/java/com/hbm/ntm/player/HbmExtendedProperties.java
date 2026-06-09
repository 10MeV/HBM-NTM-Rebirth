package com.hbm.ntm.player;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ExtPropertiesSyncPacket;
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
        ModMessages.sendToPlayer(new ExtPropertiesSyncPacket(writeSyncedData(player, chunkRadiation, resistance)), player);
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
