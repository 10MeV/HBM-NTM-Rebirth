package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmItemActionReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ItemActionPacket(InteractionHand hand, ResourceLocation actionType, CompoundTag data) {
    public ItemActionPacket {
        hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ItemActionPacket decode(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ResourceLocation actionType = buffer.readResourceLocation();
        CompoundTag tag = buffer.readNbt();
        return new ItemActionPacket(hand, actionType, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ItemActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.hand == InteractionHand.OFF_HAND);
        buffer.writeResourceLocation(packet.actionType);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ItemActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(ItemActionPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        ItemStack stack = player.getItemInHand(packet.hand);
        if (!stack.isEmpty() && stack.getItem() instanceof HbmItemActionReceiver receiver
                && receiver.canReceiveItemAction(player, packet.hand, stack, packet.actionType, packet.data)) {
            receiver.handleItemAction(player, packet.hand, stack, packet.actionType, packet.data);
            player.getInventory().setChanged();
        }
    }
}
