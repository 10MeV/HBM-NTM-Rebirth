package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientMuzzleFlashEffects;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MuzzleFlashPacket(int entityId) implements HbmPreparablePacket {
    public static MuzzleFlashPacket decode(FriendlyByteBuf buffer) {
        return new MuzzleFlashPacket(buffer.readVarInt());
    }

    public static void encode(MuzzleFlashPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
    }

    public static void handle(MuzzleFlashPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientMuzzleFlashEffects.mark(packet.entityId));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new MuzzleFlashPacket(entityId);
    }
}
