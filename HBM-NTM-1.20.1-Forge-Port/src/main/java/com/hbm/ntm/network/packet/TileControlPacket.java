package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmGuiControlSecurity;
import com.hbm.ntm.network.HbmTileSyncable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TileControlPacket(BlockPos pos, CompoundTag data) {
    public TileControlPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static TileControlPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();
        return new TileControlPacket(pos, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(TileControlPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeNbt(packet.data);
    }

    public static void handle(TileControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(TileControlPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        BlockEntity blockEntity = HbmGuiControlSecurity.validateTileControl(player, packet.pos, "tile control");
        if (!(blockEntity instanceof HbmTileSyncable syncable)) {
            return;
        }
        if (syncable.canReceiveClientControl(player, packet.data)) {
            syncable.handleClientControl(player, packet.data);
            HbmGuiControlSecurity.markChangedAndUpdate(blockEntity);
        }
    }
}
