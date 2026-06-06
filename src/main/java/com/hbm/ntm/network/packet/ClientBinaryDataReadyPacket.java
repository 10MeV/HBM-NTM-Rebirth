package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientBinaryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientBinaryDataReadyPacket(ResourceLocation channel) {
    public static ClientBinaryDataReadyPacket decode(FriendlyByteBuf buffer) {
        return new ClientBinaryDataReadyPacket(buffer.readResourceLocation());
    }

    public static void encode(ClientBinaryDataReadyPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.channel);
    }

    public static void handle(ClientBinaryDataReadyPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientBinaryData.markReady(packet.channel));
        context.setPacketHandled(true);
    }
}
