package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.ServerTileBinaryControlTransfers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public record ServerTileBinaryControlChunkPacket(
        UUID transferId,
        BlockPos pos,
        ResourceLocation channel,
        int chunkIndex,
        int chunkCount,
        byte[] payload) {
    public static final int MAX_CHUNK_BYTES = 65_536;
    private static final int MAX_CHUNKS = 512;

    public ServerTileBinaryControlChunkPacket {
        transferId = transferId == null ? new UUID(0L, 0L) : transferId;
        pos = pos == null ? BlockPos.ZERO : pos;
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public static ServerTileBinaryControlChunkPacket decode(FriendlyByteBuf buffer) {
        UUID transferId = buffer.readUUID();
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation channel = buffer.readResourceLocation();
        int chunkIndex = buffer.readVarInt();
        int chunkCount = buffer.readVarInt();
        byte[] payload = buffer.readByteArray(MAX_CHUNK_BYTES);
        return new ServerTileBinaryControlChunkPacket(transferId, pos, channel, chunkIndex, chunkCount, payload);
    }

    public static void encode(ServerTileBinaryControlChunkPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.transferId);
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.channel);
        buffer.writeVarInt(packet.chunkIndex);
        buffer.writeVarInt(packet.chunkCount);
        buffer.writeByteArray(packet.payload);
    }

    public static void handle(ServerTileBinaryControlChunkPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(ServerTileBinaryControlChunkPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (packet.chunkCount <= 0 || packet.chunkCount > MAX_CHUNKS) {
            HbmNtm.LOGGER.warn("Rejected tile binary control transfer {} from {} with invalid chunk count {}.",
                    packet.transferId, player.getGameProfile().getName(), packet.chunkCount);
            return;
        }
        byte[] fullPayload = ServerTileBinaryControlTransfers.putChunk(
                player.getUUID(),
                packet.transferId,
                packet.pos,
                packet.channel,
                packet.chunkIndex,
                packet.chunkCount,
                packet.payload,
                player.serverLevel().getGameTime());
        if (fullPayload != null) {
            ServerTileBinaryControlPacket.handleServerPayload(packet.pos, packet.channel, fullPayload, context);
        }
    }
}
