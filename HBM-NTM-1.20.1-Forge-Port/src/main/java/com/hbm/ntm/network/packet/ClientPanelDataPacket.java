package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientPanelData;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientPanelDataPacket(ResourceLocation panelType, int legacyType, CompoundTag data) implements HbmPreparablePacket {
    public ClientPanelDataPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ClientPanelDataPacket decode(FriendlyByteBuf buffer) {
        ResourceLocation panelType = buffer.readResourceLocation();
        int legacyType = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new ClientPanelDataPacket(panelType, legacyType, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ClientPanelDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.panelType);
        buffer.writeVarInt(packet.legacyType);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ClientPanelDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientPanelData.update(packet.panelType, packet.legacyType, packet.data));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ClientPanelDataPacket(panelType, legacyType, data);
    }
}
