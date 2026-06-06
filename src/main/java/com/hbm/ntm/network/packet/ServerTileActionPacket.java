package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmGuiControlSecurity;
import com.hbm.ntm.network.HbmTypedTileActionReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerTileActionPacket(BlockPos pos, ResourceLocation actionType, int value, CompoundTag data) {
    public ServerTileActionPacket {
        pos = pos == null ? BlockPos.ZERO : pos;
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ServerTileActionPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation actionType = buffer.readResourceLocation();
        int value = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new ServerTileActionPacket(pos, actionType, value, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ServerTileActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.actionType);
        buffer.writeVarInt(packet.value);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ServerTileActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(ServerTileActionPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity blockEntity = HbmGuiControlSecurity.validateTileControl(player, packet.pos,
                "typed tile action " + packet.actionType);
        if (!(blockEntity instanceof HbmTypedTileActionReceiver receiver)) {
            return;
        }
        if (receiver.canReceiveTypedTileAction(player, packet.actionType, packet.value, packet.data)) {
            receiver.handleTypedTileAction(player, packet.actionType, packet.value, packet.data);
            HbmGuiControlSecurity.markChangedAndUpdate(blockEntity);
        }
    }
}
