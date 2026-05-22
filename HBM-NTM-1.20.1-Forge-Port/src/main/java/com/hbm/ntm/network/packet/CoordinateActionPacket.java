package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmCoordinateActionReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CoordinateActionPacket(InteractionHand hand, BlockPos pos, int action, int value, int frequency, CompoundTag data) {
    public CoordinateActionPacket {
        hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        pos = pos == null ? BlockPos.ZERO : pos;
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static CoordinateActionPacket decode(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        BlockPos pos = buffer.readBlockPos();
        int action = buffer.readVarInt();
        int value = buffer.readVarInt();
        int frequency = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new CoordinateActionPacket(hand, pos, action, value, frequency, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(CoordinateActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.hand == InteractionHand.OFF_HAND);
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.action);
        buffer.writeVarInt(packet.value);
        buffer.writeVarInt(packet.frequency);
        buffer.writeNbt(packet.data);
    }

    public static void handle(CoordinateActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(CoordinateActionPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        ItemStack stack = player.getItemInHand(packet.hand);
        if (!stack.isEmpty() && stack.getItem() instanceof HbmCoordinateActionReceiver receiver
                && receiver.canReceiveCoordinateAction(player, stack, packet.pos, packet.action, packet.value, packet.frequency, packet.data)) {
            receiver.handleCoordinateAction(player, stack, packet.pos, packet.action, packet.value, packet.frequency, packet.data);
            player.getInventory().setChanged();
        }
    }
}
