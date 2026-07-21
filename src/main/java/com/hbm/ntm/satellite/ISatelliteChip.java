package com.hbm.ntm.satellite;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public interface ISatelliteChip {
    String TAG_FREQUENCY = "freq";

    static int getFrequencyFromStack(ItemStack stack) {
        if (stack != null && legacyCarrierItem(stack) instanceof ISatelliteChip chip) {
            return chip.getFrequency(stack);
        }
        return 0;
    }

    static void setFrequencyOnStack(ItemStack stack, int frequency) {
        if (stack != null && legacyCarrierItem(stack) instanceof ISatelliteChip chip) {
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
        if (stack == null) {
            return 0;
        }
        // Legacy ISatChip#getFreq creates the tag even when the frequency is
        // absent; callers historically observe that NBT side effect.  A
        // 1.7.10 ItemStack with count zero remained a mutable stack carrier,
        // while 1.20.1 refuses to attach a tag while isEmpty() is true.  Make
        // the carrier temporarily non-empty so the modern tag setter accepts
        // the source-backed NBT side effect, then restore its exact count.
        return legacyTag(stack).getInt(TAG_FREQUENCY);
    }

    default void setFrequency(ItemStack stack, int frequency) {
        if (stack != null) {
            legacyTag(stack).putInt(TAG_FREQUENCY, frequency);
        }
    }

    private static CompoundTag legacyTag(ItemStack stack) {
        if (!stack.isEmpty()) {
            return stack.getOrCreateTag();
        }
        int legacyCount = stack.getCount();
        stack.setCount(1);
        CompoundTag tag = stack.getOrCreateTag();
        stack.setCount(legacyCount);
        return tag;
    }

    /**
     * 1.20.1's public ItemStack#getItem returns AIR when count is zero, unlike
     * the 1.7.10 carrier used by ISatChip.  The underlying Forge holder still
     * retains the original item, so read it only for this legacy facade.
     */
    private static Item legacyCarrierItem(ItemStack stack) {
        // ItemStack.EMPTY itself has no Forge holder. It was null in 1.7.10
        // too, so retain the static facade's ordinary non-chip/no-op result.
        if (stack.isEmpty()) {
            // A deserialized or handler-provided empty stack can have neither
            // an item nor a Forge holder.  1.7.10 represented this as a null
            // stack, which ISatChip#getFreqS maps to frequency zero.
            return stack.delegate != null ? stack.delegate.value() : null;
        }
        return stack.getItem();
    }

    default int getFreq(ItemStack stack) {
        return getFrequency(stack);
    }

    default void setFreq(ItemStack stack, int frequency) {
        setFrequency(stack, frequency);
    }
}
