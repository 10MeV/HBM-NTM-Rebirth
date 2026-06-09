package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientHbmLivingProperties;
import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmLivingProperties.SyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRadiationSyncPacket(SyncData data) implements HbmPreparablePacket {
    public PlayerRadiationSyncPacket {
        data = data == null ? HbmLivingProperties.emptySyncedData() : data;
    }

    public static PlayerRadiationSyncPacket decode(FriendlyByteBuf buffer) {
        return new PlayerRadiationSyncPacket(HbmLivingProperties.decodeSyncedData(buffer));
    }

    public static void encode(PlayerRadiationSyncPacket packet, FriendlyByteBuf buffer) {
        HbmLivingProperties.encodeSyncedData(packet.data, buffer);
    }

    public static void handle(PlayerRadiationSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientHbmLivingProperties.update(packet.data)));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new PlayerRadiationSyncPacket(data);
    }
}
