package com.hbm.util.fauxpointtwelve;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy 1.7.10 block-position data carrier used by old explosion code.
 */
@Deprecated(forRemoval = false)
public class ChunkPosition {
    public final int chunkPosX;
    public final int chunkPosY;
    public final int chunkPosZ;

    public ChunkPosition(int chunkPosX, int chunkPosY, int chunkPosZ) {
        this.chunkPosX = chunkPosX;
        this.chunkPosY = chunkPosY;
        this.chunkPosZ = chunkPosZ;
    }

    public ChunkPosition(Vec3 vec) {
        this(Mth.floor(vec.x), Mth.floor(vec.y), Mth.floor(vec.z));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ChunkPosition other)) {
            return false;
        }
        return chunkPosX == other.chunkPosX && chunkPosY == other.chunkPosY && chunkPosZ == other.chunkPosZ;
    }

    @Override
    public int hashCode() {
        return chunkPosX * 8976890 + chunkPosY * 981131 + chunkPosZ;
    }
}
