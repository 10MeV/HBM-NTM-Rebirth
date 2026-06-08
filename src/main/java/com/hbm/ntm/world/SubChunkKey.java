package com.hbm.ntm.world;

import net.minecraft.world.level.ChunkPos;

public class SubChunkKey {
    private int chunkX;
    private int chunkZ;
    private int sectionY;
    private int hash;

    public SubChunkKey(int chunkX, int chunkZ, int sectionY) {
        update(chunkX, chunkZ, sectionY);
    }

    public SubChunkKey(ChunkPos pos, int sectionY) {
        update(pos.x, pos.z, sectionY);
    }

    public static SubChunkKey ofBlock(int blockX, int blockY, int blockZ) {
        return new SubChunkKey(blockX >> 4, blockZ >> 4, blockY >> 4);
    }

    public SubChunkKey update(int chunkX, int chunkZ, int sectionY) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.sectionY = sectionY;
        int result = sectionY;
        result = 31 * result + chunkX;
        result = 31 * result + chunkZ;
        this.hash = result;
        return this;
    }

    public int getSubY() {
        return sectionY;
    }

    public int getSectionY() {
        return sectionY;
    }

    public int getChunkXPos() {
        return chunkX;
    }

    public int getChunkZPos() {
        return chunkZ;
    }

    public ChunkPos getPos() {
        return new ChunkPos(chunkX, chunkZ);
    }

    public long chunkLong() {
        return ChunkPos.asLong(chunkX, chunkZ);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SubChunkKey other)) {
            return false;
        }
        return sectionY == other.sectionY && chunkX == other.chunkX && chunkZ == other.chunkZ;
    }
}
