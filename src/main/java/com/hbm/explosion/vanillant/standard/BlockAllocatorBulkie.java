package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;

/** Legacy facade over the modern VNT Bulkie allocator. */
@Deprecated(forRemoval = false)
public class BlockAllocatorBulkie extends com.hbm.ntm.explosion.vnt.standard.BlockAllocatorBulkie implements IBlockAllocator {
    public BlockAllocatorBulkie(double maximumResistance) { super(maximumResistance); }
    public BlockAllocatorBulkie(double maximumResistance, int resolution) { super(maximumResistance, resolution); }
}
