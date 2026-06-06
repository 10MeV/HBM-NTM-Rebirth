package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmGuiControlSecurity;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LegacyButtonPacket(BlockPos pos, int value, int id) {
    public static LegacyButtonPacket decode(FriendlyByteBuf buffer) {
        return new LegacyButtonPacket(buffer.readBlockPos(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void encode(LegacyButtonPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.value);
        buffer.writeVarInt(packet.id);
    }

    public static void handle(LegacyButtonPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(LegacyButtonPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity blockEntity = HbmGuiControlSecurity.validateTileControl(player, packet.pos, "legacy button");
        if (!(blockEntity instanceof HbmLegacyButtonReceiver receiver)) {
            return;
        }
        if (receiver.canReceiveLegacyButton(player, packet.value, packet.id)) {
            receiver.handleLegacyButton(player, packet.value, packet.id);
            HbmGuiControlSecurity.markChangedAndUpdate(blockEntity);
        }
    }
}
