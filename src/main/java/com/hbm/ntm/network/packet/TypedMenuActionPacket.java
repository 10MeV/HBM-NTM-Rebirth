package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmTypedMenuActionReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TypedMenuActionPacket(ResourceLocation actionType, int value, CompoundTag data) {
    public TypedMenuActionPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static TypedMenuActionPacket decode(FriendlyByteBuf buffer) {
        ResourceLocation actionType = buffer.readResourceLocation();
        int value = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new TypedMenuActionPacket(actionType, value, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(TypedMenuActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.actionType);
        buffer.writeVarInt(packet.value);
        buffer.writeNbt(packet.data);
    }

    public static void handle(TypedMenuActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(TypedMenuActionPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !(player.containerMenu instanceof HbmTypedMenuActionReceiver receiver)) {
            return;
        }
        if (receiver.canReceiveTypedMenuAction(player, packet.actionType, packet.value, packet.data)) {
            receiver.handleTypedMenuAction(player, packet.actionType, packet.value, packet.data);
            player.containerMenu.broadcastChanges();
        }
    }
}
