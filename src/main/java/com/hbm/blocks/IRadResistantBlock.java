package com.hbm.blocks;

/**
 * 1.7.10 radiation-pocket barrier marker used by
 * {@code ChunkRadiationHandlerNT}. The clean port does not assign this marker
 * to modern blocks unless a source-backed block migration does so explicitly.
 */
@Deprecated(forRemoval = false)
public interface IRadResistantBlock {
    int getResistance();
}
