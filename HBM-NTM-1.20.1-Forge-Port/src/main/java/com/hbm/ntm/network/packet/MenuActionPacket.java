package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmMenuActionReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MenuActionPacket(int action, int value, CompoundTag data) {
    public MenuActionPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static MenuActionPacket decode(FriendlyByteBuf buffer) {
        int action = buffer.readVarInt();
        int value = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new MenuActionPacket(action, value, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(MenuActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.action);
        buffer.writeVarInt(packet.value);
        buffer.writeNbt(packet.data);
    }

    public static void handle(MenuActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(MenuActionPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !(player.containerMenu instanceof HbmMenuActionReceiver receiver)) {
            return;
        }
        if (receiver.canReceiveMenuAction(player, packet.action, packet.value, packet.data)) {
            receiver.handleMenuAction(player, packet.action, packet.value, packet.data);
            player.containerMenu.broadcastChanges();
        }
    }
}
