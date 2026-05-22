package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmEntitySyncable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EntitySyncPacket(int entityId, CompoundTag data) {
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
        }
    }
}
