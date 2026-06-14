package com.hbm.util;


import net.minecraft.world.level.ChunkPos;

import java.util.List;

/**
 * Legacy-name chunk shape helper facade.
 */
@Deprecated(forRemoval = false)
public final class ChunkShapeHelper {
    private ChunkShapeHelper() {
    }

    public static List<ChunkPos> getChunksAlongLineSegment(int x0, int z0, int x1, int z1, double paddingSize) {
        return com.hbm.ntm.world.ChunkShapeHelper.getChunksAlongLineSegment(x0, z0, x1, z1, paddingSize);
    }
}
