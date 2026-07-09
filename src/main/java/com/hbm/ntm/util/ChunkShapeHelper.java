package com.hbm.ntm.util;

import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy-name chunk shape helper facade.
 */
@Deprecated(forRemoval = false)
public final class ChunkShapeHelper {
    private ChunkShapeHelper() {
    }

    public static List<ChunkCoordIntPair> getChunksAlongLineSegment(int x0, int z0, int x1, int z1,
            double paddingSize) {
        List<ChunkCoordIntPair> chunks = new ArrayList<>();
        for (net.minecraft.world.level.ChunkPos pos : com.hbm.ntm.world.ChunkShapeHelper
                .getChunksAlongLineSegment(x0, z0, x1, z1, paddingSize)) {
            chunks.add(new ChunkCoordIntPair(pos.x, pos.z));
        }
        return chunks;
    }
}
