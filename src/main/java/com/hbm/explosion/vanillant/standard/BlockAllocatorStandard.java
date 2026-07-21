package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;

/** Legacy facade over the modern VNT standard allocator. */
@Deprecated(forRemoval = false)
public class BlockAllocatorStandard extends com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard implements IBlockAllocator {
    public BlockAllocatorStandard() { super(); }
    public BlockAllocatorStandard(int resolution) { super(resolution); }
}
