package com.hbm.ntm.client;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmClientTileBinaryReceiver;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientTileBinarySyncRequestPacket;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientTileBinaryData {
    private static final long TRANSFER_TIMEOUT_TICKS = 20L * 30L;
    private static final long REQUEST_COOLDOWN_TICKS = 20L;
    private static final Map<UUID, ChunkAssembly> CHUNKS = new HashMap<>();
    private static final Map<RequestKey, Long> LAST_SYNC_REQUESTS = new HashMap<>();

    public static boolean putChunk(UUID transferId, BlockPos pos, ResourceLocation channel, int chunkIndex, int chunkCount, byte[] chunk, long gameTime) {
        if (chunkCount <= 0 || chunkIndex < 0 || chunkIndex >= chunkCount) {
            return false;
        }
        ChunkAssembly assembly = CHUNKS.computeIfAbsent(transferId, ignored -> new ChunkAssembly(pos, channel, chunkCount, gameTime));
        if (!assembly.accepts(pos, channel, chunkCount)) {
            CHUNKS.remove(transferId);
            return false;
        }
        assembly.put(chunkIndex, chunk, gameTime);
        if (!assembly.isComplete()) {
            return false;
        }
        dispatch(pos, channel, assembly.join());
        CHUNKS.remove(transferId);
        return true;
    }

    public static void dispatch(BlockPos pos, ResourceLocation channel, byte[] payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof HbmClientTileBinaryReceiver receiver)) {
            HbmNtm.LOGGER.debug("Tile binary data at {} had no HbmClientTileBinaryReceiver receiver.", pos);
            requestResync(level, pos, channel);
            return;
        }
        FriendlyByteBuf payloadBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload));
        try {
            receiver.handleClientTileBinaryData(channel, payloadBuffer);
        } catch (Exception exception) {
            HbmNtm.LOGGER.warn("Tile binary data receiver failed at {} for channel {}.", pos, channel, exception);
        } finally {
            payloadBuffer.release();
        }
    }

    public static int pendingTransfers() {
        return CHUNKS.size();
    }

    public static int pendingChunkCount() {
        int count = 0;
        for (ChunkAssembly assembly : CHUNKS.values()) {
            count += assembly.received;
        }
        return count;
    }

    public static int pendingClientResyncRequests() {
        return LAST_SYNC_REQUESTS.size();
    }

    public static long clientResyncRequestCooldownTicks() {
        return REQUEST_COOLDOWN_TICKS;
    }

    public static void clearAll() {
        CHUNKS.clear();
        clearClientResyncRequests();
    }

    public static void clearClientResyncRequests() {
        LAST_SYNC_REQUESTS.clear();
    }

    public static int pruneExpired(long gameTime) {
        int before = CHUNKS.size();
        CHUNKS.values().removeIf(assembly -> gameTime - assembly.lastTouchedGameTime > TRANSFER_TIMEOUT_TICKS);
        LAST_SYNC_REQUESTS.entrySet().removeIf(entry -> gameTime - entry.getValue() > TRANSFER_TIMEOUT_TICKS);
        return before - CHUNKS.size();
    }

    public static long transferTimeoutTicks() {
        return TRANSFER_TIMEOUT_TICKS;
    }

    private ClientTileBinaryData() {
    }

    private static void requestResync(ClientLevel level, BlockPos pos, ResourceLocation channel) {
        long gameTime = level.getGameTime();
        RequestKey key = new RequestKey(pos.immutable(), channel);
        Long lastRequest = LAST_SYNC_REQUESTS.get(key);
        if (lastRequest != null && gameTime - lastRequest < REQUEST_COOLDOWN_TICKS) {
            return;
        }
        LAST_SYNC_REQUESTS.put(key, gameTime);
        ModMessages.sendToServer(new ClientTileBinarySyncRequestPacket(pos, channel));
    }

    private record RequestKey(BlockPos pos, ResourceLocation channel) {
    }

    private static final class ChunkAssembly {
        private final BlockPos pos;
        private final ResourceLocation channel;
        private final byte[][] chunks;
        private int received;
        private long lastTouchedGameTime;

        private ChunkAssembly(BlockPos pos, ResourceLocation channel, int chunkCount, long gameTime) {
            this.pos = pos.immutable();
            this.channel = channel;
            this.chunks = new byte[chunkCount][];
            this.lastTouchedGameTime = gameTime;
        }

        private boolean accepts(BlockPos pos, ResourceLocation channel, int chunkCount) {
            return this.pos.equals(pos) && this.channel.equals(channel) && chunks.length == chunkCount;
        }

        private void put(int chunkIndex, byte[] chunk, long gameTime) {
            lastTouchedGameTime = gameTime;
            if (chunks[chunkIndex] == null) {
                received++;
            }
            chunks[chunkIndex] = Arrays.copyOf(chunk, chunk.length);
        }

        private boolean isComplete() {
            return received == chunks.length;
        }

        private byte[] join() {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (byte[] chunk : chunks) {
                output.writeBytes(chunk);
            }
            return output.toByteArray();
        }
    }
}
