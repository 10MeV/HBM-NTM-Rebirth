package com.hbm.ntm.client;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ClientBinaryData {
    private static final long TRANSFER_TIMEOUT_TICKS = 20L * 30L;
    private static final Map<ResourceLocation, Map<String, byte[]>> DATA = new HashMap<>();
    private static final Map<UUID, ChunkAssembly> CHUNKS = new HashMap<>();
    private static final Map<ResourceLocation, Integer> READY_VERSIONS = new HashMap<>();
    private static final List<ClientBinaryDataListener> LISTENERS = new ArrayList<>();

    public static void put(ResourceLocation channel, String name, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        DATA.computeIfAbsent(channel, ignored -> new HashMap<>())
                .put(name, safePayload);
        notifyListeners(channel, name, safePayload, false);
    }

    public static void clear(ResourceLocation channel) {
        DATA.remove(channel);
        READY_VERSIONS.remove(channel);
        CHUNKS.values().removeIf(assembly -> assembly.channel.equals(channel));
        notifyListeners(channel, "", new byte[0], true);
    }

    public static void clearAll() {
        DATA.clear();
        READY_VERSIONS.clear();
        CHUNKS.clear();
        LISTENERS.clear();
    }

    public static Optional<byte[]> get(ResourceLocation channel, String name) {
        byte[] payload = DATA.getOrDefault(channel, Map.of()).get(name);
        return payload == null ? Optional.empty() : Optional.of(Arrays.copyOf(payload, payload.length));
    }

    public static void markReady(ResourceLocation channel) {
        READY_VERSIONS.merge(channel, 1, Integer::sum);
        notifyListeners(channel, "", new byte[0], false);
    }

    public static int readyVersion(ResourceLocation channel) {
        return READY_VERSIONS.getOrDefault(channel, 0);
    }

    public static int channelCount() {
        return DATA.size();
    }

    public static int entryCount() {
        int count = 0;
        for (Map<String, byte[]> entries : DATA.values()) {
            count += entries.size();
        }
        return count;
    }

    public static int readyChannelCount() {
        return READY_VERSIONS.size();
    }

    public static boolean putChunk(UUID transferId, ResourceLocation channel, String name, int chunkIndex, int chunkCount, byte[] chunk, long gameTime) {
        if (chunkCount <= 0 || chunkIndex < 0 || chunkIndex >= chunkCount) {
            return false;
        }
        ChunkAssembly assembly = CHUNKS.computeIfAbsent(transferId, ignored -> new ChunkAssembly(channel, name, chunkCount, gameTime));
        if (!assembly.accepts(channel, name, chunkCount)) {
            CHUNKS.remove(transferId);
            return false;
        }
        assembly.put(chunkIndex, chunk, gameTime);
        if (!assembly.isComplete()) {
            return false;
        }
        put(channel, name, assembly.join());
        CHUNKS.remove(transferId);
        return true;
    }

    public static int pendingTransfers() {
        return CHUNKS.size();
    }

    public static int pruneExpired(long gameTime) {
        int before = CHUNKS.size();
        CHUNKS.values().removeIf(assembly -> gameTime - assembly.lastTouchedGameTime > TRANSFER_TIMEOUT_TICKS);
        return before - CHUNKS.size();
    }

    public static long transferTimeoutTicks() {
        return TRANSFER_TIMEOUT_TICKS;
    }

    public static void addListener(ClientBinaryDataListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientBinaryDataListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    private static void notifyListeners(ResourceLocation channel, String name, byte[] payload, boolean cleared) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        for (ClientBinaryDataListener listener : List.copyOf(LISTENERS)) {
            listener.onClientBinaryData(channel, name == null ? "" : name, Arrays.copyOf(safePayload, safePayload.length),
                    cleared, readyVersion(channel));
        }
    }

    private ClientBinaryData() {
    }

    private static final class ChunkAssembly {
        private final ResourceLocation channel;
        private final String name;
        private final byte[][] chunks;
        private int received;
        private long lastTouchedGameTime;

        private ChunkAssembly(ResourceLocation channel, String name, int chunkCount, long gameTime) {
            this.channel = channel;
            this.name = name;
            this.chunks = new byte[chunkCount][];
            this.lastTouchedGameTime = gameTime;
        }

        private boolean accepts(ResourceLocation channel, String name, int chunkCount) {
            return this.channel.equals(channel) && this.name.equals(name) && chunks.length == chunkCount;
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
