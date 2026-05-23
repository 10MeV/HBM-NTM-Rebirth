package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientBinaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public record ClientBinaryDataChunkPacket(
        UUID transferId,
        ResourceLocation channel,
        String name,
        int chunkIndex,
        int chunkCount,
        byte[] payload) {
    public static final int MAX_CHUNK_BYTES = 262_144;
    private static final int MAX_CHUNKS = 512;

    public ClientBinaryDataChunkPacket {
        transferId = transferId == null ? new UUID(0L, 0L) : transferId;
        name = name == null ? "" : name;
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public static ClientBinaryDataChunkPacket decode(FriendlyByteBuf buffer) {
        UUID transferId = buffer.readUUID();
        ResourceLocation channel = buffer.readResourceLocation();
        String name = buffer.readUtf();
        int chunkIndex = buffer.readVarInt();
        int chunkCount = buffer.readVarInt();
        byte[] payload = buffer.readByteArray(MAX_CHUNK_BYTES);
        return new ClientBinaryDataChunkPacket(transferId, channel, name, chunkIndex, chunkCount, payload);
    }

    public static void encode(ClientBinaryDataChunkPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.transferId);
        buffer.writeResourceLocation(packet.channel);
        buffer.writeUtf(packet.name);
        buffer.writeVarInt(packet.chunkIndex);
        buffer.writeVarInt(packet.chunkCount);
        buffer.writeByteArray(packet.payload);
    }

    public static void handle(ClientBinaryDataChunkPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.chunkCount <= 0 || packet.chunkCount > MAX_CHUNKS) {
                HbmNtm.LOGGER.warn("Rejected client binary data transfer {} with invalid chunk count {}.",
                        packet.transferId, packet.chunkCount);
                return;
            }
            ClientBinaryData.putChunk(packet.transferId, packet.channel, packet.name,
                    packet.chunkIndex, packet.chunkCount, packet.payload, clientGameTime());
        });
        context.setPacketHandled(true);
    }

    private static long clientGameTime() {
        return Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
    }
}
