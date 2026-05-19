package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientRadiationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRadiationSyncPacket(float radiation, float digamma, float environment, float chunkRadiation, float resistance) {
    public static PlayerRadiationSyncPacket decode(FriendlyByteBuf buffer) {
        return new PlayerRadiationSyncPacket(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void encode(PlayerRadiationSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.radiation);
        buffer.writeFloat(packet.digamma);
        buffer.writeFloat(packet.environment);
        buffer.writeFloat(packet.chunkRadiation);
        buffer.writeFloat(packet.resistance);
    }

    public static void handle(PlayerRadiationSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientRadiationData.update(packet.radiation, packet.digamma, packet.environment, packet.chunkRadiation, packet.resistance)));
        context.setPacketHandled(true);
    }
}
