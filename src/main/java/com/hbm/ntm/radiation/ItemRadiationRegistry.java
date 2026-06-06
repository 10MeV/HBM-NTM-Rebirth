package com.hbm.ntm.radiation;

import net.minecraft.world.item.ItemStack;

public final class ItemRadiationRegistry {
    public static void registerDefaults() {
        HazardRegistry.registerDefaults();
    }

    public static float getRadiation(ItemStack stack) {
        return HazardRegistry.getHazardLevel(stack, HazardType.RADIATION);
    }

    public static float getHazardLevel(ItemStack stack, HazardType type) {
        return HazardRegistry.getHazardLevel(stack, type);
    }

    private ItemRadiationRegistry() {
    }
}
