package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmItemControlReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ItemControlPacket(InteractionHand hand, CompoundTag data) {
    public ItemControlPacket {
        hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ItemControlPacket decode(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        CompoundTag tag = buffer.readNbt();
        return new ItemControlPacket(hand, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ItemControlPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.hand == InteractionHand.OFF_HAND);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ItemControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(ItemControlPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        ItemStack stack = player.getItemInHand(packet.hand);
        if (!stack.isEmpty() && stack.getItem() instanceof HbmItemControlReceiver receiver
                && receiver.canReceiveItemControl(player, stack, packet.data)) {
            receiver.handleItemControl(player, stack, packet.data);
            player.getInventory().setChanged();
        }
    }
}
