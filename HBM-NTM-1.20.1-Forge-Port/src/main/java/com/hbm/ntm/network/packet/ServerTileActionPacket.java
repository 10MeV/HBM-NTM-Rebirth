package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmTypedTileActionReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerTileActionPacket(BlockPos pos, ResourceLocation actionType, int value, CompoundTag data) {
    private static final double MAX_DISTANCE_SQ = 16.0D * 16.0D;

    public ServerTileActionPacket {
        pos = pos == null ? BlockPos.ZERO : pos;
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ServerTileActionPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation actionType = buffer.readResourceLocation();
        int value = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new ServerTileActionPacket(pos, actionType, value, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ServerTileActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.actionType);
        buffer.writeVarInt(packet.value);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ServerTileActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(ServerTileActionPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote typed tile action {} from {} at {}",
                    packet.actionType, player.getGameProfile().getName(), packet.pos);
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.hasChunk(packet.pos.getX() >> 4, packet.pos.getZ() >> 4)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos);
        if (blockEntity instanceof HbmTypedTileActionReceiver receiver
                && receiver.canReceiveTypedTileAction(player, packet.actionType, packet.value, packet.data)) {
            receiver.handleTypedTileAction(player, packet.actionType, packet.value, packet.data);
            blockEntity.setChanged();
            level.sendBlockUpdated(packet.pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }
}
