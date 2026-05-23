package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientTileBinaryData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public record ClientTileBinaryDataPacket(BlockPos pos, ResourceLocation channel, byte[] payload) {
    public static final int MAX_PAYLOAD_BYTES = 1_048_576;

    public ClientTileBinaryDataPacket {
        pos = pos == null ? BlockPos.ZERO : pos;
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public static ClientTileBinaryDataPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation channel = buffer.readResourceLocation();
        byte[] payload = buffer.readByteArray(MAX_PAYLOAD_BYTES);
        return new ClientTileBinaryDataPacket(pos, channel, payload);
    }

    public static void encode(ClientTileBinaryDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.channel);
        buffer.writeByteArray(packet.payload);
    }

    public static void handle(ClientTileBinaryDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleClient(packet));
        context.setPacketHandled(true);
    }

    private static void handleClient(ClientTileBinaryDataPacket packet) {
        ClientTileBinaryData.dispatch(packet.pos, packet.channel, packet.payload);
    }
}
