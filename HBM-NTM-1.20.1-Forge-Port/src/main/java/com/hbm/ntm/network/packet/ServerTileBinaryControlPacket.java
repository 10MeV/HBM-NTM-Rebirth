package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmTileBinaryControlReceiver;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public record ServerTileBinaryControlPacket(BlockPos pos, ResourceLocation channel, byte[] payload) {
    public static final int MAX_PAYLOAD_BYTES = 262_144;
    private static final double MAX_DISTANCE_SQ = 16.0D * 16.0D;

    public ServerTileBinaryControlPacket {
        pos = pos == null ? BlockPos.ZERO : pos;
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public static ServerTileBinaryControlPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation channel = buffer.readResourceLocation();
        byte[] payload = buffer.readByteArray(MAX_PAYLOAD_BYTES);
        return new ServerTileBinaryControlPacket(pos, channel, payload);
    }

    public static void encode(ServerTileBinaryControlPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.channel);
        buffer.writeByteArray(packet.payload);
    }

    public static void handle(ServerTileBinaryControlPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    static void handleServerPayload(BlockPos pos, ResourceLocation channel, byte[] payload, NetworkEvent.Context context) {
        handleServer(new ServerTileBinaryControlPacket(pos, channel, payload), context);
    }

    private static void handleServer(ServerTileBinaryControlPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote tile binary control from {} at {}", player.getGameProfile().getName(), packet.pos);
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!level.hasChunk(packet.pos.getX() >> 4, packet.pos.getZ() >> 4)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos);
        if (!(blockEntity instanceof HbmTileBinaryControlReceiver receiver)) {
            return;
        }
        if (!receiver.canReceiveClientTileBinaryData(player, packet.channel, packet.payload.length)) {
            HbmNtm.LOGGER.warn("Blocked tile binary control channel {} from {} at {}",
                    packet.channel, player.getGameProfile().getName(), packet.pos);
            return;
        }
        FriendlyByteBuf payloadBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(packet.payload));
        try {
            receiver.handleClientTileBinaryData(player, packet.channel, payloadBuffer);
            blockEntity.setChanged();
            level.sendBlockUpdated(packet.pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        } catch (Exception exception) {
            HbmNtm.LOGGER.warn("Tile binary control receiver failed at {} for channel {}.", packet.pos, packet.channel, exception);
        } finally {
            payloadBuffer.release();
        }
    }
}
