package com.hbm.items;

import com.hbm.ntm.satellite.ISatelliteChip;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy package facade for 1.7.10 satellite-frequency item contracts.
 */
@Deprecated(forRemoval = false)
public interface ISatChip extends ISatelliteChip {
    static int getFreqS(ItemStack stack) {
        return ISatelliteChip.getFrequencyFromStack(stack);
    }

    static void setFreqS(ItemStack stack, int frequency) {
        ISatelliteChip.setFrequencyOnStack(stack, frequency);
    }
}
