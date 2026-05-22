package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientBinaryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public record ClientBinaryDataPacket(ResourceLocation channel, String name, byte[] payload, boolean clearChannel) {
    private static final int MAX_PAYLOAD_BYTES = 1_048_576;

    public ClientBinaryDataPacket {
        name = name == null ? "" : name;
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public static ClientBinaryDataPacket decode(FriendlyByteBuf buffer) {
        ResourceLocation channel = buffer.readResourceLocation();
        boolean clearChannel = buffer.readBoolean();
        String name = buffer.readUtf();
        byte[] payload = clearChannel ? new byte[0] : buffer.readByteArray(MAX_PAYLOAD_BYTES);
        return new ClientBinaryDataPacket(channel, name, payload, clearChannel);
    }

    public static void encode(ClientBinaryDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.channel);
        buffer.writeBoolean(packet.clearChannel);
        buffer.writeUtf(packet.name);
        if (!packet.clearChannel) {
            buffer.writeByteArray(packet.payload);
        }
    }

    public static void handle(ClientBinaryDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.clearChannel) {
                ClientBinaryData.clear(packet.channel);
            } else {
                ClientBinaryData.put(packet.channel, packet.name, packet.payload);
            }
        });
        context.setPacketHandled(true);
    }
}
