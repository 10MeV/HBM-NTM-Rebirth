package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientPollutionData;
import com.hbm.ntm.client.ClientPermaSyncData;
import com.hbm.ntm.client.ClientTomImpactData;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PermaSyncPacket(CompoundTag data) implements HbmPreparablePacket {
    public PermaSyncPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static PermaSyncPacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new PermaSyncPacket(tag == null ? new CompoundTag() : tag);
    }

    public static void encode(PermaSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data);
    }

    public static void handle(PermaSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientPermaSyncData.update(packet.data);
            ClientPollutionData.updateFromPermaSync(packet.data);
            ClientTomImpactData.updateFromPermaSync(packet.data);
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new PermaSyncPacket(data);
    }
}
