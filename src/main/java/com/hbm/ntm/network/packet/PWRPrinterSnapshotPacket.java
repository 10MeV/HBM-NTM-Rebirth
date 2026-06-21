package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.screen.PWRSlicePrinterScreen;
import com.hbm.ntm.network.HbmPreparablePacket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

public record PWRPrinterSnapshotPacket(BlockPos min, BlockPos max, Direction direction, List<BlockState> states)
        implements HbmPreparablePacket {
    public PWRPrinterSnapshotPacket {
        min = min == null ? BlockPos.ZERO : min.immutable();
        max = max == null ? BlockPos.ZERO : max.immutable();
        direction = direction == null ? Direction.NORTH : direction;
        states = states == null ? List.of() : List.copyOf(states);
    }

    public static PWRPrinterSnapshotPacket decode(FriendlyByteBuf buffer) {
        BlockPos min = buffer.readBlockPos();
        BlockPos max = buffer.readBlockPos();
        Direction direction = buffer.readEnum(Direction.class);
        int count = buffer.readVarInt();
        List<BlockState> states = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), buffer.readNbt()));
        }
        return new PWRPrinterSnapshotPacket(min, max, direction, states);
    }

    public static void encode(PWRPrinterSnapshotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.min);
        buffer.writeBlockPos(packet.max);
        buffer.writeEnum(packet.direction);
        buffer.writeVarInt(packet.states.size());
        for (BlockState state : packet.states) {
            buffer.writeNbt(NbtUtils.writeBlockState(state));
        }
    }

    public static void handle(PWRPrinterSnapshotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> PWRSlicePrinterScreen.open(packet));
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new PWRPrinterSnapshotPacket(min, max, direction, states);
    }
}
