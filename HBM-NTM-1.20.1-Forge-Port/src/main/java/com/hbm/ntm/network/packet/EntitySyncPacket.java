package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmEntitySyncable;
import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record EntitySyncPacket(int entityId, CompoundTag data) implements HbmPreparablePacket {
    private static final long REQUEST_COOLDOWN_TICKS = 20L;
    private static final Map<Integer, Long> LAST_SYNC_REQUESTS = new HashMap<>();

    public EntitySyncPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static EntitySyncPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new EntitySyncPacket(entityId, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(EntitySyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeNbt(packet.data);
    }

    public static void handle(EntitySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        context.setPacketHandled(true);
    }

    private static void handleClient(EntitySyncPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(packet.entityId);
        if (entity instanceof HbmEntitySyncable syncable) {
            syncable.handleClientSyncTag(packet.data);
        } else {
            HbmNtm.LOGGER.debug("Entity sync packet for id {} had no HbmEntitySyncable receiver.", packet.entityId);
            requestResync(level, packet.entityId);
        }
    }

    private static void requestResync(ClientLevel level, int entityId) {
        long gameTime = level.getGameTime();
        Long lastRequest = LAST_SYNC_REQUESTS.get(entityId);
        if (lastRequest != null && gameTime - lastRequest < REQUEST_COOLDOWN_TICKS) {
            return;
        }
        LAST_SYNC_REQUESTS.put(entityId, gameTime);
        ModMessages.sendToServer(new EntitySyncRequestPacket(entityId));
    }

    public static void clearClientResyncRequests() {
        LAST_SYNC_REQUESTS.clear();
    }

    public static int pendingClientResyncRequests() {
        return LAST_SYNC_REQUESTS.size();
    }

    public static long clientResyncRequestCooldownTicks() {
        return REQUEST_COOLDOWN_TICKS;
    }

    @Override
    public Object prepareForThreadedSend() {
        return new EntitySyncPacket(entityId, data);
    }
}
