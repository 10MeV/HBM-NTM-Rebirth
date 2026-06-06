package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmEntitySyncable;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EntitySyncRequestPacket(int entityId) {
    private static final double MAX_DISTANCE_SQ = 64.0D * 64.0D;

    public static EntitySyncRequestPacket decode(FriendlyByteBuf buffer) {
        return new EntitySyncRequestPacket(buffer.readVarInt());
    }

    public static void encode(EntitySyncRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
    }

    public static void handle(EntitySyncRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(EntitySyncRequestPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        Entity entity = player.serverLevel().getEntity(packet.entityId);
        if (entity == null || entity.distanceToSqr(player) > MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote entity sync request from {} for id {}",
                    player.getGameProfile().getName(), packet.entityId);
            return;
        }
        if (entity instanceof HbmEntitySyncable syncable && syncable.canSendClientSyncTo(player)) {
            ModMessages.syncEntityToPlayer(syncable, entity, player);
        }
    }
}
