package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.explosion.vanillant.interfaces.IBlockProcessor;
import com.hbm.explosion.vanillant.interfaces.IDropChanceMutator;
import com.hbm.explosion.vanillant.interfaces.IFortuneMutator;

/** Legacy facade over the modern standard block processor. */
@Deprecated(forRemoval = false)
public class BlockProcessorStandard extends com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard implements IBlockProcessor {
    public BlockProcessorStandard withChance(IDropChanceMutator chance) { super.withChance(chance); return this; }
    public BlockProcessorStandard withFortune(IFortuneMutator fortune) { super.withFortune(fortune); return this; }
    public BlockProcessorStandard withBlockEffect(IBlockMutator mutator) { super.withBlockEffect(mutator); return this; }
    @Override public BlockProcessorStandard setNoDrop() { super.setNoDrop(); return this; }
    @Override public BlockProcessorStandard setAllDrop() { super.setAllDrop(); return this; }
    @Override public BlockProcessorStandard setFortune(int fortune) { super.setFortune(fortune); return this; }
}
