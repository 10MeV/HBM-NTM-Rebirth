package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockMutator;

/** Legacy facade over the modern fire block mutator. */
@Deprecated(forRemoval = false)
public class BlockMutatorFire extends com.hbm.ntm.explosion.vnt.standard.BlockMutatorFire implements IBlockMutator {
    public BlockMutatorFire() { super(); }
}
