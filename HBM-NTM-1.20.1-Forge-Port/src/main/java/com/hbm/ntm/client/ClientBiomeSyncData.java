package com.hbm.ntm.client;

import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientBiomeSyncData {
    private static final Map<ChunkPos, short[]> CHUNK_BIOMES = new HashMap<>();

    public static void updateCell(int chunkX, int chunkZ, int blockX, int blockZ, short biome) {
        short[] biomes = CHUNK_BIOMES.computeIfAbsent(new ChunkPos(chunkX, chunkZ), ignored -> new short[256]);
        biomes[(blockZ & 15) << 4 | (blockX & 15)] = biome;
    }

    public static void updateChunk(int chunkX, int chunkZ, short[] biomes) {
        if (biomes == null || biomes.length != 256) {
            return;
        }
        CHUNK_BIOMES.put(new ChunkPos(chunkX, chunkZ), Arrays.copyOf(biomes, biomes.length));
    }

    public static Optional<short[]> getChunk(int chunkX, int chunkZ) {
        short[] biomes = CHUNK_BIOMES.get(new ChunkPos(chunkX, chunkZ));
        return biomes == null ? Optional.empty() : Optional.of(Arrays.copyOf(biomes, biomes.length));
    }

    public static void clearAll() {
        CHUNK_BIOMES.clear();
    }

    private ClientBiomeSyncData() {
    }
}
