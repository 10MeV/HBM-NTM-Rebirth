package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LegacyButtonPacket(BlockPos pos, int value, int id) {
    private static final double MAX_DISTANCE_SQ = 16.0D * 16.0D;

    public static LegacyButtonPacket decode(FriendlyByteBuf buffer) {
        return new LegacyButtonPacket(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void encode(LegacyButtonPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.value);
        buffer.writeVarInt(packet.id);
    }

    public static void handle(LegacyButtonPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(LegacyButtonPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote legacy button packet from {} at {}", player.getGameProfile().getName(), packet.pos);
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.hasChunk(packet.pos.getX() >> 4, packet.pos.getZ() >> 4)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos);
        if (blockEntity instanceof HbmLegacyButtonReceiver receiver
                && receiver.canReceiveLegacyButton(player, packet.value, packet.id)) {
            receiver.handleLegacyButton(player, packet.value, packet.id);
            blockEntity.setChanged();
            level.sendBlockUpdated(packet.pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }
}
