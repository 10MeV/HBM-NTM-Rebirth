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
    public static final int MAX_SNAPSHOT_STATES = 4096;
    public static final int MAX_SCREENSHOT_SLICES = 64;
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
        validateBounds(min, max);
        if (count < 0 || count > MAX_SNAPSHOT_STATES || count != snapshotVolume(min, max)) {
            throw new IllegalArgumentException("Invalid PWR printer snapshot state count: " + count);
        }
        List<BlockState> states = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            states.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), buffer.readNbt()));
        }
        return new PWRPrinterSnapshotPacket(min, max, direction, states);
    }

    public static void encode(PWRPrinterSnapshotPacket packet, FriendlyByteBuf buffer) {
        if (!isValidSnapshot(packet)) {
            throw new IllegalArgumentException("Refusing to encode an oversized PWR printer snapshot");
        }
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

    public static boolean isValidSnapshot(PWRPrinterSnapshotPacket packet) {
        return packet != null
                && packet.direction != null
                && packet.states != null
                && isValidBounds(packet.min, packet.max)
                && packet.states.size() == snapshotVolume(packet.min, packet.max);
    }

    public static boolean isValidBounds(BlockPos min, BlockPos max) {
        if (min == null || max == null) {
            return false;
        }
        long sizeY = Math.abs((long) max.getY() - min.getY()) + 1L;
        return sizeY <= MAX_SCREENSHOT_SLICES && snapshotVolumeLong(min, max) <= MAX_SNAPSHOT_STATES;
    }

    public static int snapshotVolume(BlockPos min, BlockPos max) {
        long volume = snapshotVolumeLong(min, max);
        if (volume > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("PWR printer snapshot volume exceeds integer range");
        }
        return (int) volume;
    }

    private static long snapshotVolumeLong(BlockPos min, BlockPos max) {
        if (min == null || max == null) {
            return Long.MAX_VALUE;
        }
        long sizeX = Math.abs((long) max.getX() - min.getX()) + 1L;
        long sizeY = Math.abs((long) max.getY() - min.getY()) + 1L;
        long sizeZ = Math.abs((long) max.getZ() - min.getZ()) + 1L;
        if (sizeX > MAX_SNAPSHOT_STATES || sizeY > MAX_SNAPSHOT_STATES || sizeZ > MAX_SNAPSHOT_STATES) {
            return Long.MAX_VALUE;
        }
        long xy = sizeX * sizeY;
        return xy > MAX_SNAPSHOT_STATES || sizeZ > MAX_SNAPSHOT_STATES / xy
                ? Long.MAX_VALUE
                : xy * sizeZ;
    }

    private static void validateBounds(BlockPos min, BlockPos max) {
        if (!isValidBounds(min, max)) {
            throw new IllegalArgumentException("Invalid or oversized PWR printer snapshot bounds");
        }
    }

    @Override
    public Object prepareForThreadedSend() {
        return new PWRPrinterSnapshotPacket(min, max, direction, states);
    }
}
