package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import net.minecraft.world.level.block.Block;

/** Legacy facade over the modern debris block mutator. */
@Deprecated(forRemoval = false)
public class BlockMutatorDebris extends com.hbm.ntm.explosion.vnt.standard.BlockMutatorDebris implements IBlockMutator {
    public BlockMutatorDebris(Block block) { super(block); }
    public BlockMutatorDebris(Block block, int meta) { super(block, meta); }
}
