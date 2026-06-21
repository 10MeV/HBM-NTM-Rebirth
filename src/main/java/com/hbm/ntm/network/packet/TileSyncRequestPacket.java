package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TileSyncRequestPacket(BlockPos pos) {
    private static final double MAX_DISTANCE_SQ = 32.0D * 32.0D;

    public static TileSyncRequestPacket decode(FriendlyByteBuf buffer) {
        return new TileSyncRequestPacket(buffer.readBlockPos());
    }

    public static void encode(TileSyncRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static void handle(TileSyncRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(TileSyncRequestPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!level.hasChunk(packet.pos.getX() >> 4, packet.pos.getZ() >> 4)) {
            return;
        }
        BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, packet.pos);
        BlockPos receiverPos = blockEntity == null ? packet.pos : blockEntity.getBlockPos();
        if (player.distanceToSqr(receiverPos.getX() + 0.5D, receiverPos.getY() + 0.5D,
                receiverPos.getZ() + 0.5D) > MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote tile sync request from {} at {} resolved to {}",
                    player.getGameProfile().getName(), packet.pos, receiverPos);
            return;
        }
        if (blockEntity instanceof HbmTileSyncable syncable && syncable.canSendClientSyncTo(player)) {
            ModMessages.syncTileToPlayer(syncable, blockEntity, player);
        }
    }
}
