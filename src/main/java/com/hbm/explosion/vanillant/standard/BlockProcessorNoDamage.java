package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.explosion.vanillant.interfaces.IBlockProcessor;

/** Legacy facade over the modern no-damage block processor. */
@Deprecated(forRemoval = false)
public class BlockProcessorNoDamage extends com.hbm.ntm.explosion.vnt.standard.BlockProcessorNoDamage implements IBlockProcessor {
    public BlockProcessorNoDamage withBlockEffect(IBlockMutator mutator) { super.withBlockEffect(mutator); return this; }
}
