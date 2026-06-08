package com.hbm.ntm.api.tile;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface FactoryMachine {
    boolean isStructureValid(Level level);

    long getPowerScaled(long scale);

    int getProgressScaled(int scale);

    boolean isProcessable(ItemStack stack);
}
