package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmClientEntityEventReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientEntityEventPacket(int entityId, ResourceLocation eventType, CompoundTag data) {
    public ClientEntityEventPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ClientEntityEventPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        ResourceLocation eventType = buffer.readResourceLocation();
        CompoundTag tag = buffer.readNbt();
        return new ClientEntityEventPacket(entityId, eventType, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ClientEntityEventPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeResourceLocation(packet.eventType);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ClientEntityEventPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        context.setPacketHandled(true);
    }

    private static void handleClient(ClientEntityEventPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(packet.entityId);
        if (entity instanceof HbmClientEntityEventReceiver receiver) {
            receiver.handleClientEntityEvent(packet.eventType, packet.data);
        } else {
            HbmNtm.LOGGER.debug("Client entity event {} for id {} had no HbmClientEntityEventReceiver receiver.",
                    packet.eventType, packet.entityId);
        }
    }
}
