package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientRadiationData;
import com.hbm.ntm.client.ClientRadiationData.PlayerRadiationSyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRadiationSyncPacket(float radiation, float digamma, float environment, float chunkRadiation, float resistance,
        int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire) {
    public static PlayerRadiationSyncPacket decode(FriendlyByteBuf buffer) {
        return new PlayerRadiationSyncPacket(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void encode(PlayerRadiationSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.radiation);
        buffer.writeFloat(packet.digamma);
        buffer.writeFloat(packet.environment);
        buffer.writeFloat(packet.chunkRadiation);
        buffer.writeFloat(packet.resistance);
        buffer.writeVarInt(packet.asbestos);
        buffer.writeVarInt(packet.blackLung);
        buffer.writeVarInt(packet.bombTimer);
        buffer.writeVarInt(packet.contagion);
        buffer.writeVarInt(packet.oil);
        buffer.writeVarInt(packet.fire);
        buffer.writeVarInt(packet.phosphorus);
        buffer.writeVarInt(packet.balefire);
        buffer.writeVarInt(packet.blackFire);
    }

    public static void handle(PlayerRadiationSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientRadiationData.update(new PlayerRadiationSyncData(packet.radiation, packet.digamma, packet.environment, packet.chunkRadiation, packet.resistance,
                        packet.asbestos, packet.blackLung, packet.bombTimer, packet.contagion, packet.oil,
                        packet.fire, packet.phosphorus, packet.balefire, packet.blackFire))));
        context.setPacketHandled(true);
    }
}
