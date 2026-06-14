package com.hbm.util;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

/**
 * Legacy 1.7.10 package bridge for sub-chunk coordinate keys.
 */
@Deprecated(forRemoval = false)
public class SubChunkKey extends com.hbm.ntm.util.SubChunkKey {
    public SubChunkKey(int chunkX, int chunkZ, int sectionY) {
        super(chunkX, chunkZ, sectionY);
    }

    public SubChunkKey(ChunkPos pos, int sectionY) {
        super(pos, sectionY);
    }

    public static SubChunkKey ofBlock(int blockX, int blockY, int blockZ) {
        com.hbm.ntm.util.SubChunkKey key = com.hbm.ntm.util.SubChunkKey.ofBlock(blockX, blockY, blockZ);
        return new SubChunkKey(key.getChunkXPos(), key.getChunkZPos(), key.getSectionY());
    }

    public static SubChunkKey ofBlock(net.minecraft.core.BlockPos pos) {
        return ofBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public static SubChunkKey ofSection(SectionPos pos) {
        return new SubChunkKey(pos.x(), pos.z(), pos.y());
    }
}
