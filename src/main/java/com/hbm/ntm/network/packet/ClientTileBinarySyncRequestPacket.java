package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmTileBinarySyncProvider;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientTileBinarySyncRequestPacket(BlockPos pos, ResourceLocation channel) {
    private static final double MAX_DISTANCE_SQ = 32.0D * 32.0D;

    public ClientTileBinarySyncRequestPacket {
        pos = pos == null ? BlockPos.ZERO : pos;
    }

    public static ClientTileBinarySyncRequestPacket decode(FriendlyByteBuf buffer) {
        return new ClientTileBinarySyncRequestPacket(buffer.readBlockPos(), buffer.readResourceLocation());
    }

    public static void encode(ClientTileBinarySyncRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.channel);
    }

    public static void handle(ClientTileBinarySyncRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(ClientTileBinarySyncRequestPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote tile binary sync request from {} at {} for channel {}",
                    player.getGameProfile().getName(), packet.pos, packet.channel);
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!level.hasChunk(packet.pos.getX() >> 4, packet.pos.getZ() >> 4)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos);
        if (blockEntity instanceof HbmTileBinarySyncProvider provider
                && provider.canSendClientTileBinaryDataTo(player, packet.channel)) {
            ModMessages.syncTileBinaryToPlayer(provider, blockEntity, player, packet.channel);
        }
    }
}
