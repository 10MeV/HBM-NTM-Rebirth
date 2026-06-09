package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientLegacyItemAnimationHandler;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LegacyItemAnimationPacket(short animationType, int receiverIndex, int itemIndex) implements HbmPreparablePacket {
    public static LegacyItemAnimationPacket decode(FriendlyByteBuf buffer) {
        return new LegacyItemAnimationPacket(buffer.readShort(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void encode(LegacyItemAnimationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeShort(packet.animationType);
        buffer.writeVarInt(packet.receiverIndex);
        buffer.writeVarInt(packet.itemIndex);
    }

    public static void handle(LegacyItemAnimationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientLegacyItemAnimationHandler.handle(packet.animationType, packet.receiverIndex, packet.itemIndex));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new LegacyItemAnimationPacket(animationType, receiverIndex, itemIndex);
    }
}
