package com.hbm.util.fauxpointtwelve;

/**
 * Legacy 1.7.10 chunk coordinate pair data carrier.
 */
@Deprecated(forRemoval = false)
public class ChunkCoordIntPair {
    public final int chunkXPos;
    public final int chunkZPos;

    public ChunkCoordIntPair(int chunkXPos, int chunkZPos) {
        this.chunkXPos = chunkXPos;
        this.chunkZPos = chunkZPos;
    }

    public static long chunkXZ2Int(int chunkX, int chunkZ) {
        return (long) chunkX & 4294967295L | ((long) chunkZ & 4294967295L) << 32;
    }

    public int getCenterXPos() {
        return (chunkXPos << 4) + 8;
    }

    public int getCenterZPosition() {
        return (chunkZPos << 4) + 8;
    }

    public ChunkPosition func_151349_a(int y) {
        return new ChunkPosition(getCenterXPos(), y, getCenterZPosition());
    }

    @Override
    public int hashCode() {
        int xHash = 1664525 * chunkXPos + 1013904223;
        int zHash = 1664525 * (chunkZPos ^ -559038737) + 1013904223;
        return xHash ^ zHash;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ChunkCoordIntPair other)) {
            return false;
        }
        return chunkXPos == other.chunkXPos && chunkZPos == other.chunkZPos;
    }

    @Override
    public String toString() {
        return "[" + chunkXPos + ", " + chunkZPos + "]";
    }
}
