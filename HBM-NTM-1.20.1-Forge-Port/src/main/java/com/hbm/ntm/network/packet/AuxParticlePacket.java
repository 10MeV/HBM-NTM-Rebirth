package com.hbm.ntm.network.packet;

import com.hbm.ntm.particle.ClientParticleBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AuxParticlePacket(CompoundTag data) {
    public static AuxParticlePacket decode(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new AuxParticlePacket(tag == null ? new CompoundTag() : tag);
    }

    public static void encode(AuxParticlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data);
    }

    public static void handle(AuxParticlePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientParticleBridge.handleAux(packet.data));
        context.setPacketHandled(true);
    }
}
