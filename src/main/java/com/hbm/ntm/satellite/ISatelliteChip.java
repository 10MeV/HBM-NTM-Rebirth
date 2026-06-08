package com.hbm.ntm.satellite;

import net.minecraft.world.item.ItemStack;

public interface ISatelliteChip {
    String TAG_FREQUENCY = "freq";

    static int getFrequencyFromStack(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ISatelliteChip chip) {
            return chip.getFrequency(stack);
        }
        return 0;
    }

    static void setFrequencyOnStack(ItemStack stack, int frequency) {
        if (!stack.isEmpty() && stack.getItem() instanceof ISatelliteChip chip) {
            chip.setFrequency(stack, frequency);
        }
    }

    default int getFrequency(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) {
            return 0;
        }
        return stack.getTag().getInt(TAG_FREQUENCY);
    }

    default void setFrequency(ItemStack stack, int frequency) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putInt(TAG_FREQUENCY, frequency);
        }
    }
}
