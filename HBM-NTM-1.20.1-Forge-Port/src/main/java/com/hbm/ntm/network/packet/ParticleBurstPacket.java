package com.hbm.ntm.network.packet;

import com.hbm.ntm.particle.ClientParticleBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ParticleBurstPacket(BlockPos pos, BlockState state) {
    public static ParticleBurstPacket decode(FriendlyByteBuf buffer) {
        return new ParticleBurstPacket(buffer.readBlockPos(), Block.BLOCK_STATE_REGISTRY.byId(buffer.readVarInt()));
    }

    public static void encode(ParticleBurstPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(Block.getId(packet.state));
    }

    public static void handle(ParticleBurstPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.state != null) {
                ClientParticleBridge.burst(packet.pos, packet.state);
            }
        });
        context.setPacketHandled(true);
    }
}
