package com.hbm.ntm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public interface HbmTileBinarySyncProvider {
    Map<BlockEntity, HbmTileBinarySyncState> LEGACY_NETWORK_PACK_NT_STATES =
            Collections.synchronizedMap(new WeakHashMap<>());

    default ResourceLocation getClientTileBinarySyncChannel() {
        return HbmNetworkActions.BUF_PACKET;
    }

    default boolean canSendClientTileBinaryDataTo(ServerPlayer player, ResourceLocation channel) {
        return getClientTileBinarySyncChannel().equals(channel);
    }

    void writeClientTileBinaryData(FriendlyByteBuf data);

    default boolean networkPackNT(int range) {
        return networkPackNT((double) range);
    }

    default boolean networkPackNT(double range) {
        if (!(this instanceof BlockEntity blockEntity)) {
            return false;
        }
        return ModMessages.networkPackNT(this, blockEntity, range, getLegacyNetworkPackNtSyncState(blockEntity));
    }

    default boolean networkPackNT(ResourceLocation channel, int range) {
        return networkPackNT(channel, (double) range);
    }

    default boolean networkPackNT(ResourceLocation channel, double range) {
        if (!(this instanceof BlockEntity blockEntity)) {
            return false;
        }
        return ModMessages.networkPackNT(this, blockEntity, channel, range, getLegacyNetworkPackNtSyncState(blockEntity));
    }

    default HbmTileBinarySyncState getLegacyNetworkPackNtSyncState(BlockEntity blockEntity) {
        return LEGACY_NETWORK_PACK_NT_STATES.computeIfAbsent(blockEntity, ignored -> new HbmTileBinarySyncState());
    }
}
