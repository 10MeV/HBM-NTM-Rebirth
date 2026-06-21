package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FuelPoolRecipes {
    private FuelPoolRecipes() {
    }

    public static boolean isInput(ItemStack stack) {
        return !stack.isEmpty() && recipes().containsKey(stack.getItem());
    }

    public static ItemStack cool(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Item output = recipes().get(stack.getItem());
        return output == null ? ItemStack.EMPTY : new ItemStack(output, 1);
    }

    public static boolean canExtract(ItemStack stack) {
        return !isInput(stack);
    }

    public static Map<Item, Item> recipes() {
        Map<Item, Item> recipes = new LinkedHashMap<>();
        int count = Math.min(ModItems.PWR_FUEL_HOT_ITEMS.size(), ModItems.PWR_FUEL_DEPLETED_ITEMS.size());
        for (int i = 0; i < count; i++) {
            recipes.put(ModItems.PWR_FUEL_HOT_ITEMS.get(i).get(), ModItems.PWR_FUEL_DEPLETED_ITEMS.get(i).get());
        }
        return Map.copyOf(recipes);
    }
}
