package com.hbm.ntm.satellite;

import net.minecraft.world.item.ItemStack;

public interface ISatelliteChip {
    String TAG_FREQUENCY = "freq";

    static int getFrequencyFromStack(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ISatelliteChip chip) {
            return chip.getFrequency(stack);
        }
        return 0;
    }

    static void setFrequencyOnStack(ItemStack stack, int frequency) {
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ISatelliteChip chip) {
            chip.setFrequency(stack, frequency);
        }
    }

    static int getFreqS(ItemStack stack) {
        return getFrequencyFromStack(stack);
    }

    static void setFreqS(ItemStack stack, int frequency) {
        setFrequencyOnStack(stack, frequency);
    }

    default int getFrequency(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getTag() == null) {
            return 0;
        }
        return stack.getTag().getInt(TAG_FREQUENCY);
    }

    default void setFrequency(ItemStack stack, int frequency) {
        if (stack != null && !stack.isEmpty()) {
            stack.getOrCreateTag().putInt(TAG_FREQUENCY, frequency);
        }
    }

    default int getFreq(ItemStack stack) {
        return getFrequency(stack);
    }

    default void setFreq(ItemStack stack, int frequency) {
        setFrequency(stack, frequency);
    }
}
