package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientInformMessages;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientInformPacket(Component message, int id, int millis) implements HbmPreparablePacket {
    public ClientInformPacket {
        message = message == null ? Component.empty() : message;
    }

    public static ClientInformPacket decode(FriendlyByteBuf buffer) {
        return new ClientInformPacket(buffer.readComponent(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void encode(ClientInformPacket packet, FriendlyByteBuf buffer) {
        buffer.writeComponent(packet.message);
        buffer.writeVarInt(packet.id);
        buffer.writeVarInt(packet.millis);
    }

    public static void handle(ClientInformPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientInformMessages.show(packet.message, packet.id, packet.millis));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ClientInformPacket(message, id, millis);
    }
}
