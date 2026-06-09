package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientHbmLivingProperties;
import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.player.HbmExtendedProperties;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ExtPropertiesSyncPacket(HbmExtendedProperties.SyncData data) implements HbmPreparablePacket {
    public ExtPropertiesSyncPacket {
        data = data == null ? HbmExtendedProperties.emptySyncedData() : data;
    }

    public static ExtPropertiesSyncPacket decode(FriendlyByteBuf buffer) {
        HbmLivingProperties.SyncData living = HbmLivingProperties.decodeSyncedData(buffer);
        CompoundTag player = buffer.readNbt();
        return new ExtPropertiesSyncPacket(new HbmExtendedProperties.SyncData(
                living,
                HbmPlayerProperties.readSyncedData(player)));
    }

    public static void encode(ExtPropertiesSyncPacket packet, FriendlyByteBuf buffer) {
        HbmLivingProperties.encodeSyncedData(packet.data.living(), buffer);
        buffer.writeNbt(packet.data.player().toTag());
    }

    public static void handle(ExtPropertiesSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientHbmLivingProperties.update(packet.data.living());
            ClientHbmPlayerProperties.update(HbmPlayerProperties.DATA_TYPE, packet.data.player().toTag());
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ExtPropertiesSyncPacket(data);
    }
}
