package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmEntityActionReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerEntityActionPacket(int entityId, ResourceLocation actionType, CompoundTag data) {
    private static final double MAX_DISTANCE_SQ = 64.0D * 64.0D;

    public ServerEntityActionPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ServerEntityActionPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        ResourceLocation actionType = buffer.readResourceLocation();
        CompoundTag tag = buffer.readNbt();
        return new ServerEntityActionPacket(entityId, actionType, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ServerEntityActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeResourceLocation(packet.actionType);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ServerEntityActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(ServerEntityActionPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        Entity entity = player.serverLevel().getEntity(packet.entityId);
        if (entity == null || entity.distanceToSqr(player) > MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote entity action {} from {} for id {}",
                    packet.actionType, player.getGameProfile().getName(), packet.entityId);
            return;
        }
        if (entity instanceof HbmEntityActionReceiver receiver
                && receiver.canReceiveEntityAction(player, packet.actionType, packet.data)) {
            receiver.handleEntityAction(player, packet.actionType, packet.data);
        }
    }
}
