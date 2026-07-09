package com.hbm.inventory.recipes.anvil;

import net.minecraft.world.item.ItemStack;

/**
 * Legacy package DTO surface for addon recipe registration.
 */
@Deprecated(forRemoval = false)
public final class AnvilRecipes {
    private AnvilRecipes() {
    }

    public static final class AnvilOutput {
        public ItemStack stack;
        public float chance;

        public AnvilOutput(ItemStack stack) {
            this(stack, 1.0F);
        }

        public AnvilOutput(ItemStack stack, float chance) {
            this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
            this.chance = chance;
        }
    }

    public enum OverlayType {
        NONE,
        CONSTRUCTION,
        RECYCLING,
        SMITHING
    }
}
