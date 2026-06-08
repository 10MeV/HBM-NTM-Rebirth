package com.hbm.ntm.radiation;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

public final class RadiationShieldingRegistry {
    private static final Map<Item, Float> RESISTANCE = new IdentityHashMap<>();

    public static void register(Item item, float resistance) {
        RESISTANCE.put(item, Math.max(0.0F, resistance));
    }

    public static void clear() {
        RESISTANCE.clear();
    }

    public static float getResistance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0F;
        }
        return RESISTANCE.getOrDefault(stack.getItem(), 0.0F);
    }

    private RadiationShieldingRegistry() {
    }
}
