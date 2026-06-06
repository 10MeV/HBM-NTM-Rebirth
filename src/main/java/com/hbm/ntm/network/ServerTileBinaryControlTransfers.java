package com.hbm.ntm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ServerTileBinaryControlTransfers {
    private static final long TRANSFER_TIMEOUT_TICKS = 20L * 30L;
    private static final Map<TransferKey, ChunkAssembly> CHUNKS = new HashMap<>();

    public static synchronized byte[] putChunk(UUID playerId, UUID transferId, BlockPos pos, ResourceLocation channel,
                                               int chunkIndex, int chunkCount, byte[] chunk, long gameTime) {
        if (chunkCount <= 0 || chunkIndex < 0 || chunkIndex >= chunkCount) {
            return null;
        }
        TransferKey key = new TransferKey(playerId, transferId);
        ChunkAssembly assembly = CHUNKS.computeIfAbsent(key, ignored -> new ChunkAssembly(pos, channel, chunkCount, gameTime));
        if (!assembly.accepts(pos, channel, chunkCount)) {
            CHUNKS.remove(key);
            return null;
        }
        assembly.put(chunkIndex, chunk, gameTime);
        if (!assembly.isComplete()) {
            return null;
        }
        CHUNKS.remove(key);
        return assembly.join();
    }

    public static synchronized void clearPlayer(UUID playerId) {
        CHUNKS.keySet().removeIf(key -> key.playerId.equals(playerId));
    }

    public static synchronized int pendingTransfers() {
        return CHUNKS.size();
    }

    public static synchronized int pruneExpired(long gameTime) {
        int before = CHUNKS.size();
        CHUNKS.values().removeIf(assembly -> gameTime - assembly.lastTouchedGameTime > TRANSFER_TIMEOUT_TICKS);
        return before - CHUNKS.size();
    }

    public static long transferTimeoutTicks() {
        return TRANSFER_TIMEOUT_TICKS;
    }

    private ServerTileBinaryControlTransfers() {
    }

    private record TransferKey(UUID playerId, UUID transferId) {
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
