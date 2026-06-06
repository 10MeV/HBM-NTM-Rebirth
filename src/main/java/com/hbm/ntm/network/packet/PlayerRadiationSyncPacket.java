package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientRadiationData;
import com.hbm.ntm.client.ClientRadiationData.ContaminationEffectData;
import com.hbm.ntm.client.ClientRadiationData.PlayerRadiationSyncData;
import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.radiation.RadiationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PlayerRadiationSyncPacket(float radiation, float digamma, float environment, float chunkRadiation, float resistance,
        int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire,
        List<RadiationData.ContaminationEffect> contaminationEffects) implements HbmPreparablePacket {
    public PlayerRadiationSyncPacket {
        contaminationEffects = List.copyOf(contaminationEffects);
    }

    public static PlayerRadiationSyncPacket decode(FriendlyByteBuf buffer) {
        float radiation = buffer.readFloat();
        float digamma = buffer.readFloat();
        float environment = buffer.readFloat();
        float chunkRadiation = buffer.readFloat();
        float resistance = buffer.readFloat();
        int asbestos = buffer.readVarInt();
        int blackLung = buffer.readVarInt();
        int bombTimer = buffer.readVarInt();
        int contagion = buffer.readVarInt();
        int oil = buffer.readVarInt();
        int fire = buffer.readVarInt();
        int phosphorus = buffer.readVarInt();
        int balefire = buffer.readVarInt();
        int blackFire = buffer.readVarInt();
        int contaminationCount = buffer.readVarInt();
        List<RadiationData.ContaminationEffect> contaminationEffects = new ArrayList<>(contaminationCount);
        for (int i = 0; i < contaminationCount; i++) {
            contaminationEffects.add(new RadiationData.ContaminationEffect(
                    buffer.readFloat(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean()));
        }
        return new PlayerRadiationSyncPacket(radiation, digamma, environment, chunkRadiation, resistance,
                asbestos, blackLung, bombTimer, contagion, oil, fire, phosphorus, balefire, blackFire, contaminationEffects);
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
        buffer.writeVarInt(packet.contaminationEffects.size());
        for (RadiationData.ContaminationEffect effect : packet.contaminationEffects) {
            buffer.writeFloat(effect.maxRad());
            buffer.writeVarInt(effect.maxTime());
            buffer.writeVarInt(effect.time());
            buffer.writeBoolean(effect.ignoreArmor());
        }
    }

    public static void handle(PlayerRadiationSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientRadiationData.update(new PlayerRadiationSyncData(packet.radiation, packet.digamma, packet.environment, packet.chunkRadiation, packet.resistance,
                        packet.asbestos, packet.blackLung, packet.bombTimer, packet.contagion, packet.oil,
                        packet.fire, packet.phosphorus, packet.balefire, packet.blackFire,
                        packet.contaminationEffects.stream()
                                .map(effect -> new ContaminationEffectData(effect.maxRad(), effect.maxTime(), effect.time(), effect.ignoreArmor()))
                                .toList()))));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new PlayerRadiationSyncPacket(radiation, digamma, environment, chunkRadiation, resistance,
                asbestos, blackLung, bombTimer, contagion, oil, fire, phosphorus, balefire, blackFire,
                contaminationEffects);
    }
}
