package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface HbmTileSyncable {
    default CompoundTag getClientSyncTag() {
        return new CompoundTag();
    }

    default boolean canSendClientSyncTo(ServerPlayer player) {
        return true;
    }

    default void handleClientSyncTag(CompoundTag tag) {
    }

    default boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return true;
    }

    default void handleClientControl(ServerPlayer player, CompoundTag tag) {
    }

    default boolean syncToTracking() {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.syncTileToTracking(this, blockEntity);
            return true;
        }
        return false;
    }

    default boolean syncToTrackingThreaded() {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.syncTileToTrackingThreaded(this, blockEntity);
            return true;
        }
        return false;
    }

    default boolean syncToPlayer(ServerPlayer player) {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.syncTileToPlayer(this, blockEntity, player);
            return true;
        }
        return false;
    }

    default boolean syncToPlayerThreaded(ServerPlayer player) {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.syncTileToPlayerThreaded(this, blockEntity, player);
            return true;
        }
        return false;
    }
}
