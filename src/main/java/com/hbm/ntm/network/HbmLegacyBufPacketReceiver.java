package com.hbm.ntm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface HbmLegacyBufPacketReceiver extends HbmTileBinarySyncProvider, HbmClientTileBinaryReceiver {
    @Override
    default ResourceLocation getClientTileBinarySyncChannel() {
        return HbmNetworkActions.BUF_PACKET;
    }

    @Override
    default void writeClientTileBinaryData(FriendlyByteBuf data) {
        serializeLegacyBufPacket(data);
    }

    @Override
    default void handleClientTileBinaryData(ResourceLocation channel, FriendlyByteBuf data) {
        if (getClientTileBinarySyncChannel().equals(channel)) {
            deserializeLegacyBufPacket(data);
        }
    }

    default void serializeLegacyBufPacket(FriendlyByteBuf data) {
        serialize(data);
    }

    default void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        deserialize(data);
    }

    default void serialize(FriendlyByteBuf data) {
    }

    default void deserialize(FriendlyByteBuf data) {
    }

    default boolean sendBufPacket() {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.sendBufPacket(blockEntity);
            return true;
        }
        return false;
    }

    default boolean sendBufPacket(int range) {
        return sendBufPacket((double) range);
    }

    default boolean sendBufPacket(double range) {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.sendBufPacket(blockEntity, range);
            return true;
        }
        return false;
    }

    default boolean sendBufPacketThreaded() {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.sendBufPacketThreaded(blockEntity);
            return true;
        }
        return false;
    }

    default boolean sendBufPacketThreaded(int range) {
        return sendBufPacketThreaded((double) range);
    }

    default boolean sendBufPacketThreaded(double range) {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.sendBufPacketThreaded(blockEntity, range);
            return true;
        }
        return false;
    }

    default boolean sendBufPacket(ServerPlayer player) {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.sendBufPacket(player, blockEntity);
            return true;
        }
        return false;
    }

    default boolean sendBufPacketThreaded(ServerPlayer player) {
        if (this instanceof BlockEntity blockEntity) {
            ModMessages.sendBufPacketThreaded(player, blockEntity);
            return true;
        }
        return false;
    }
}
