package com.hbm.ntm.network.packet;

import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MachineBatteryButtonPacket(BlockPos pos, int button) {
    public static final int BUTTON_RED_LOW = 0;
    public static final int BUTTON_RED_HIGH = 1;
    public static final int BUTTON_PRIORITY = 2;

    public static MachineBatteryButtonPacket decode(FriendlyByteBuf buffer) {
        return new MachineBatteryButtonPacket(buffer.readBlockPos(), buffer.readVarInt());
    }

    public static void encode(MachineBatteryButtonPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeVarInt(packet.button);
    }

    public static void handle(MachineBatteryButtonPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            Level level = player.level();
            BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, packet.pos);
            BlockPos receiverPos = blockEntity == null ? packet.pos : blockEntity.getBlockPos();
            if (player.distanceToSqr(receiverPos.getX() + 0.5D, receiverPos.getY() + 0.5D,
                    receiverPos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            if (blockEntity instanceof MachineBatteryBlockEntity battery) {
                battery.handleClientControl(player, MachineBatteryBlockEntity.controlTag(packet.button));
                level.sendBlockUpdated(battery.getBlockPos(), battery.getBlockState(), battery.getBlockState(), 3);
            }
        });
        context.setPacketHandled(true);
    }
}
