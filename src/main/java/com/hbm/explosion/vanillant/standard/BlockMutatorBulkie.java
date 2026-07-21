package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import net.minecraft.world.level.block.Block;

/** Legacy facade over the modern Bulkie block mutator. */
@Deprecated(forRemoval = false)
public class BlockMutatorBulkie extends com.hbm.ntm.explosion.vnt.standard.BlockMutatorBulkie implements IBlockMutator {
    public BlockMutatorBulkie(Block block) { super(block); }
    public BlockMutatorBulkie(Block block, int meta) { super(block, meta); }
}
