package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientHbmLivingProperties;
import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.player.HbmExtendedProperties;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ExtPropertiesSyncPacket(HbmExtendedProperties.SyncData data) implements HbmPreparablePacket {
    public ExtPropertiesSyncPacket {
        data = data == null ? HbmExtendedProperties.emptySyncedData() : data;
    }

    public static ExtPropertiesSyncPacket decode(FriendlyByteBuf buffer) {
        return new ExtPropertiesSyncPacket(HbmExtendedProperties.decodeSyncedData(buffer));
    }

    public static void encode(ExtPropertiesSyncPacket packet, FriendlyByteBuf buffer) {
        HbmExtendedProperties.encodeSyncedData(packet.data, buffer);
    }

    public static void handle(ExtPropertiesSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientHbmLivingProperties.update(packet.data.living());
            ClientHbmPlayerProperties.update(packet.data.player());
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ExtPropertiesSyncPacket(data);
    }
}
