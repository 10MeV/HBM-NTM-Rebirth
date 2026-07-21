package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;

/** Legacy facade over the modern water VNT allocator. */
@Deprecated(forRemoval = false)
public class BlockAllocatorWater extends com.hbm.ntm.explosion.vnt.standard.BlockAllocatorWater implements IBlockAllocator {
    public BlockAllocatorWater(int resolution) { super(resolution); }
}
