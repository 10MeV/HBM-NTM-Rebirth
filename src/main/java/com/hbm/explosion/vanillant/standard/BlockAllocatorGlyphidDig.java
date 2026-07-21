package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;

/** Legacy facade over the modern glyphid-dig VNT allocator. */
@Deprecated(forRemoval = false)
public class BlockAllocatorGlyphidDig extends com.hbm.ntm.explosion.vnt.standard.BlockAllocatorGlyphidDig implements IBlockAllocator {
    public BlockAllocatorGlyphidDig(double maximumResistance) { super(maximumResistance); }
    public BlockAllocatorGlyphidDig(double maximumResistance, int resolution) { super(maximumResistance, resolution); }
}
