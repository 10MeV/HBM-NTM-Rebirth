package com.hbm.ntm.radiation;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class NbtRadiationHazardTransformer implements HazardTransformer {
    public static final String RAD_KEY = "hfrHazRadiation";

    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        if (stack.hasTag() && stack.getTag().contains(RAD_KEY)) {
            entries.add(new HazardEntry(HazardType.RADIATION, stack.getTag().getFloat(RAD_KEY)));
        }
    }
}
