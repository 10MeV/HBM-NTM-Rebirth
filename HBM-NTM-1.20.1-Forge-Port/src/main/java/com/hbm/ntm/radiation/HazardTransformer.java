package com.hbm.ntm.radiation;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface HazardTransformer {
    default void transformPre(ItemStack stack, List<HazardEntry> entries) {
    }

    default void transformPost(ItemStack stack, List<HazardEntry> entries) {
    }
}
