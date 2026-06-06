package com.hbm.ntm.client;

import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ClientBiomeSyncData {
    private static final Map<ChunkPos, short[]> CHUNK_BIOMES = new HashMap<>();
    private static final List<ClientBiomeSyncDataListener> LISTENERS = new ArrayList<>();

    public static void updateCell(int chunkX, int chunkZ, int blockX, int blockZ, short biome) {
        short[] biomes = CHUNK_BIOMES.computeIfAbsent(new ChunkPos(chunkX, chunkZ), ignored -> new short[256]);
        biomes[(blockZ & 15) << 4 | (blockX & 15)] = biome;
        notifyListeners(chunkX, chunkZ, biomes, false);
    }

    public static void updateChunk(int chunkX, int chunkZ, short[] biomes) {
        if (biomes == null || biomes.length != 256) {
            return;
        }
        short[] safeBiomes = Arrays.copyOf(biomes, biomes.length);
        CHUNK_BIOMES.put(new ChunkPos(chunkX, chunkZ), safeBiomes);
        notifyListeners(chunkX, chunkZ, safeBiomes, true);
    }

    public static Optional<short[]> getChunk(int chunkX, int chunkZ) {
        short[] biomes = CHUNK_BIOMES.get(new ChunkPos(chunkX, chunkZ));
        return biomes == null ? Optional.empty() : Optional.of(Arrays.copyOf(biomes, biomes.length));
    }

    public static int chunkCount() {
        return CHUNK_BIOMES.size();
    }

    public static void clearAll() {
        CHUNK_BIOMES.clear();
        LISTENERS.clear();
    }

    public static void addListener(ClientBiomeSyncDataListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientBiomeSyncDataListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    private static void notifyListeners(int chunkX, int chunkZ, short[] biomes, boolean fullChunk) {
        short[] snapshot = Arrays.copyOf(biomes, biomes.length);
        for (ClientBiomeSyncDataListener listener : List.copyOf(LISTENERS)) {
            listener.onClientBiomeSyncData(chunkX, chunkZ, Arrays.copyOf(snapshot, snapshot.length), fullChunk);
        }
    }

    private ClientBiomeSyncData() {
    }
}
