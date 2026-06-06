package com.hbm.ntm.client;

@FunctionalInterface
public interface ClientBiomeSyncDataListener {
    void onClientBiomeSyncData(int chunkX, int chunkZ, short[] biomes, boolean fullChunk);
}
