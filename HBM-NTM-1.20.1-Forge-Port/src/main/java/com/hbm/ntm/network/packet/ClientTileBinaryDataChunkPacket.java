package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientTileBinaryData;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public record ClientTileBinaryDataChunkPacket(
        UUID transferId,
        BlockPos pos,
        ResourceLocation channel,
        int chunkIndex,
        int chunkCount,
        byte[] payload) implements HbmPreparablePacket {
    public static final int MAX_CHUNK_BYTES = 262_144;
    private static final int MAX_CHUNKS = 512;

    public ClientTileBinaryDataChunkPacket {
        transferId = transferId == null ? new UUID(0L, 0L) : transferId;
        pos = pos == null ? BlockPos.ZERO : pos;
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public static ClientTileBinaryDataChunkPacket decode(FriendlyByteBuf buffer) {
        UUID transferId = buffer.readUUID();
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation channel = buffer.readResourceLocation();
        int chunkIndex = buffer.readVarInt();
        int chunkCount = buffer.readVarInt();
        byte[] payload = buffer.readByteArray(MAX_CHUNK_BYTES);
        return new ClientTileBinaryDataChunkPacket(transferId, pos, channel, chunkIndex, chunkCount, payload);
    }

    public static void encode(ClientTileBinaryDataChunkPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.transferId);
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.channel);
        buffer.writeVarInt(packet.chunkIndex);
        buffer.writeVarInt(packet.chunkCount);
        buffer.writeByteArray(packet.payload);
    }

    public static void handle(ClientTileBinaryDataChunkPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.chunkCount <= 0 || packet.chunkCount > MAX_CHUNKS) {
                HbmNtm.LOGGER.warn("Rejected tile binary data transfer {} with invalid chunk count {}.",
                        packet.transferId, packet.chunkCount);
                return;
            }
            ClientTileBinaryData.putChunk(packet.transferId, packet.pos, packet.channel,
                    packet.chunkIndex, packet.chunkCount, packet.payload, clientGameTime());
        });
        context.setPacketHandled(true);
    }

    private static long clientGameTime() {
        return Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ClientTileBinaryDataChunkPacket(transferId, pos, channel, chunkIndex, chunkCount, payload);
    }
}
