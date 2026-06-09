package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerPropertiesPacket(ResourceLocation dataType, CompoundTag data) implements HbmPreparablePacket {
    public PlayerPropertiesPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static PlayerPropertiesPacket decode(FriendlyByteBuf buffer) {
        ResourceLocation dataType = buffer.readResourceLocation();
        CompoundTag tag = buffer.readNbt();
        return new PlayerPropertiesPacket(dataType, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(PlayerPropertiesPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dataType);
        buffer.writeNbt(packet.data);
    }

    public static void handle(PlayerPropertiesPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientHbmPlayerProperties.update(packet.dataType, packet.data));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new PlayerPropertiesPacket(dataType, data);
    }
}
