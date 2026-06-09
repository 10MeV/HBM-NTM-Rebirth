package com.hbm.ntm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    public static List<SubChunkKey> betweenBlocks(BlockPos first, BlockPos second) {
        int minSectionX = SectionPos.blockToSectionCoord(Math.min(first.getX(), second.getX()));
        int maxSectionX = SectionPos.blockToSectionCoord(Math.max(first.getX(), second.getX()));
        int minSectionY = SectionPos.blockToSectionCoord(Math.min(first.getY(), second.getY()));
        int maxSectionY = SectionPos.blockToSectionCoord(Math.max(first.getY(), second.getY()));
        int minSectionZ = SectionPos.blockToSectionCoord(Math.min(first.getZ(), second.getZ()));
        int maxSectionZ = SectionPos.blockToSectionCoord(Math.max(first.getZ(), second.getZ()));
        return betweenSections(minSectionX, maxSectionX, minSectionY, maxSectionY, minSectionZ, maxSectionZ);
    }

    public static List<SubChunkKey> betweenBuildHeight(Level level, BlockPos first, BlockPos second) {
        int minY = WorldUtil.clampToBuildHeight(level, Math.min(first.getY(), second.getY()));
        int maxY = WorldUtil.clampToBuildHeight(level, Math.max(first.getY(), second.getY()));
        return betweenBlocks(new BlockPos(first.getX(), minY, first.getZ()),
                new BlockPos(second.getX(), maxY, second.getZ()));
    }

    public static List<SubChunkKey> aroundSphere(Level level, BlockPos origin, int radius) {
        return aroundSphere(level, origin, radius, 14);
    }

    public static List<SubChunkKey> aroundSphere(Level level, BlockPos origin, int radius, int margin) {
        int safeRadius = Math.max(0, radius);
        int safeMargin = Math.max(0, margin);
        int chunkRadius = (safeRadius + 15) >> 4;
        int originSectionX = SectionPos.blockToSectionCoord(origin.getX());
        int originSectionY = SectionPos.blockToSectionCoord(origin.getY());
        int originSectionZ = SectionPos.blockToSectionCoord(origin.getZ());
        int minSectionY = Math.max(WorldUtil.minSectionY(level),
                SectionPos.blockToSectionCoord(origin.getY() - safeRadius));
        int maxSectionY = Math.min(WorldUtil.maxSectionY(level),
                SectionPos.blockToSectionCoord(origin.getY() + safeRadius));
        long maxDistance = (long) safeRadius + safeMargin;
        long maxDistanceSquared = maxDistance * maxDistance;

        List<SubChunkKey> keys = new ArrayList<>();
        for (int chunkX = originSectionX - chunkRadius; chunkX <= originSectionX + chunkRadius; chunkX++) {
            for (int chunkZ = originSectionZ - chunkRadius; chunkZ <= originSectionZ + chunkRadius; chunkZ++) {
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    int centerX = SectionPos.sectionToBlockCoord(chunkX) + 8;
                    int centerY = SectionPos.sectionToBlockCoord(sectionY) + 8;
                    int centerZ = SectionPos.sectionToBlockCoord(chunkZ) + 8;
                    long dx = centerX - origin.getX();
                    long dy = centerY - origin.getY();
                    long dz = centerZ - origin.getZ();
                    if (dx * dx + dy * dy + dz * dz <= maxDistanceSquared) {
                        keys.add(new SubChunkKey(chunkX, chunkZ, sectionY));
                    }
                }
            }
        }
        keys.sort(Comparator.comparingInt(key -> {
            int dx = key.getChunkXPos() - originSectionX;
            int dy = key.getSectionY() - originSectionY;
            int dz = key.getChunkZPos() - originSectionZ;
            return dx * dx + dy * dy + dz * dz;
        }));
        return keys;
    }

    public static List<SubChunkKey> betweenSections(int minSectionX, int maxSectionX, int minSectionY,
                                                    int maxSectionY, int minSectionZ, int maxSectionZ) {
        List<SubChunkKey> keys = new ArrayList<>();
        for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    keys.add(new SubChunkKey(sectionX, sectionZ, sectionY));
                }
            }
        }
        return keys;
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
