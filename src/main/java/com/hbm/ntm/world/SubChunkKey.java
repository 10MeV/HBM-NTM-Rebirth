package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
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
        return new SubChunkKey(SectionPos.blockToSectionCoord(blockX),
                SectionPos.blockToSectionCoord(blockZ),
                SectionPos.blockToSectionCoord(blockY));
    }

    public static SubChunkKey ofBlock(BlockPos pos) {
        return ofBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public static SubChunkKey ofSection(SectionPos pos) {
        return new SubChunkKey(pos.x(), pos.z(), pos.y());
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

    public SectionPos sectionPos() {
        return SectionPos.of(chunkX, sectionY, chunkZ);
    }

    public long sectionLong() {
        return SectionPos.asLong(chunkX, sectionY, chunkZ);
    }

    public int getMinBlockX() {
        return SectionPos.sectionToBlockCoord(chunkX);
    }

    public int getMinBlockY() {
        return SectionPos.sectionToBlockCoord(sectionY);
    }

    public int getMinBlockZ() {
        return SectionPos.sectionToBlockCoord(chunkZ);
    }

    public int getMaxBlockX() {
        return getMinBlockX() + 15;
    }

    public int getMaxBlockY() {
        return getMinBlockY() + 15;
    }

    public int getMaxBlockZ() {
        return getMinBlockZ() + 15;
    }

    public boolean containsBlock(BlockPos pos) {
        return containsBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean containsBlock(int blockX, int blockY, int blockZ) {
        return SectionPos.blockToSectionCoord(blockX) == chunkX
                && SectionPos.blockToSectionCoord(blockY) == sectionY
                && SectionPos.blockToSectionCoord(blockZ) == chunkZ;
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

    @Override
    public String toString() {
        return "SubChunkKey[" + chunkX + "," + sectionY + "," + chunkZ + "]";
    }
}
