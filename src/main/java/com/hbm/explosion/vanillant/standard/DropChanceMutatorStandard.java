package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IDropChanceMutator;

/** Legacy facade over the modern fixed drop-chance mutator. */
@Deprecated(forRemoval = false)
public class DropChanceMutatorStandard extends com.hbm.ntm.explosion.vnt.standard.DropChanceMutatorStandard implements IDropChanceMutator {
    public DropChanceMutatorStandard(float chance) { super(chance); }
}
