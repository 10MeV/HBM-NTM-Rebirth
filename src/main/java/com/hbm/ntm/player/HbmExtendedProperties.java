package com.hbm.ntm.player;

import com.hbm.ntm.network.ModMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class HbmExtendedProperties {
    public static SyncData writeSyncedData(ServerPlayer player, float chunkRadiation, float resistance) {
        return new SyncData(
                HbmLivingProperties.writeSyncedData(player, chunkRadiation, resistance),
                HbmPlayerProperties.writePlayerSyncData(player));
    }

    public static CompoundTag writeSyncedDataTag(ServerPlayer player, float chunkRadiation, float resistance) {
        return writeSyncedData(player, chunkRadiation, resistance).toTag();
    }

    public static SyncData emptySyncedData() {
        return new SyncData(HbmLivingProperties.emptySyncedData(), HbmPlayerProperties.emptySyncedData());
    }

    public static LegacySyncData writeLegacySyncedData(ServerPlayer player) {
        return new LegacySyncData(
                HbmLivingProperties.writeLegacySyncedData(player),
                HbmPlayerProperties.writePlayerSyncData(player));
    }

    public static LegacySyncData emptyLegacySyncedData() {
        return new LegacySyncData(HbmLivingProperties.emptyLegacySyncedData(), HbmPlayerProperties.emptySyncedData());
    }

    public static void encodeSyncedData(SyncData data, FriendlyByteBuf buffer) {
        SyncData safeData = data == null ? emptySyncedData() : data;
        HbmLivingProperties.encodeSyncedData(safeData.living(), buffer);
        HbmPlayerProperties.encodeSyncedData(safeData.player(), buffer);
    }

    public static SyncData decodeSyncedData(FriendlyByteBuf buffer) {
        return new SyncData(
                HbmLivingProperties.decodeSyncedData(buffer),
                HbmPlayerProperties.decodeSyncedData(buffer));
    }

    public static void encodeLegacySyncedData(LegacySyncData data, FriendlyByteBuf buffer) {
        LegacySyncData safeData = data == null ? emptyLegacySyncedData() : data;
        HbmLivingProperties.encodeLegacySyncedData(safeData.living(), buffer);
        HbmPlayerProperties.encodeLegacySyncedData(safeData.player(), buffer);
    }

    public static LegacySyncData decodeLegacySyncedData(FriendlyByteBuf buffer) {
        return new LegacySyncData(
                HbmLivingProperties.decodeLegacySyncedData(buffer),
                HbmPlayerProperties.decodeLegacySyncedData(buffer));
    }

    public static SyncData readSyncedData(CompoundTag data) {
        CompoundTag safeData = data == null ? new CompoundTag() : data;
        CompoundTag living = safeData.contains("living", Tag.TAG_COMPOUND)
                ? safeData.getCompound("living")
                : new CompoundTag();
        CompoundTag player = safeData.contains("player", Tag.TAG_COMPOUND)
                ? safeData.getCompound("player")
                : new CompoundTag();
        return new SyncData(
                HbmLivingProperties.readSyncedData(living),
                HbmPlayerProperties.readSyncedData(player));
    }

    public static void serializeSyncedData(ServerPlayer player, FriendlyByteBuf buffer, float chunkRadiation, float resistance) {
        encodeSyncedData(writeSyncedData(player, chunkRadiation, resistance), buffer);
    }

    public static void deserializeSyncedData(Player player, FriendlyByteBuf buffer) {
        applySyncedData(player, decodeSyncedData(buffer));
    }

    public static void deserializeSyncedData(Player player, CompoundTag data) {
        applySyncedData(player, readSyncedData(data));
    }

    public static void serializeLegacySyncedData(ServerPlayer player, FriendlyByteBuf buffer) {
        encodeLegacySyncedData(writeLegacySyncedData(player), buffer);
    }

    public static void deserializeLegacySyncedData(Player player, FriendlyByteBuf buffer) {
        applyLegacySyncedData(player, decodeLegacySyncedData(buffer));
    }

    public static void applySyncedData(Player player, SyncData data) {
        if (player == null) {
            return;
        }
        SyncData safeData = data == null ? emptySyncedData() : data;
        HbmLivingProperties.applySyncedData(player, safeData.living());
        HbmPlayerProperties.applySyncedData(player, safeData.player());
    }

    public static void applyLegacySyncedData(Player player, LegacySyncData data) {
        if (player == null) {
            return;
        }
        LegacySyncData safeData = data == null ? emptyLegacySyncedData() : data;
        HbmLivingProperties.applyLegacySyncedData(player, safeData.living());
        HbmPlayerProperties.applyLegacySyncedData(player, safeData.player());
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

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.put("living", living.toTag());
            tag.put("player", player.toTag());
            return tag;
        }
    }

    public record LegacySyncData(HbmLivingProperties.LegacySyncData living, HbmPlayerProperties.SyncData player) {
        public LegacySyncData {
            living = living == null ? HbmLivingProperties.emptyLegacySyncedData() : living;
            player = player == null ? HbmPlayerProperties.emptySyncedData() : player;
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.put("living", living.toTag());
            tag.put("player", player.toTag());
            return tag;
        }
    }

    private HbmExtendedProperties() {
    }
}
