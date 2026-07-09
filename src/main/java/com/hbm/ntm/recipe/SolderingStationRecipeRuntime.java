package com.hbm.ntm.recipe;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SolderingStationRecipeRuntime {
    private SolderingStationRecipeRuntime() {
    }

    public static SolderingStationRecipe find(Level level, ItemStack[] toppings, ItemStack[] pcb, ItemStack solder) {
        if (level == null) {
            return null;
        }
        return recipes(level).stream()
                .filter(recipe -> recipe.matches(toppings, pcb, solder))
                .findFirst()
                .orElse(null);
    }

    public static boolean matchesGroup(ItemStack[] stacks, List<HbmIngredient> ingredients) {
        List<HbmIngredient> safeIngredients = ingredients == null ? List.of() : ingredients;
        ItemStack[] safeStacks = stacks == null ? new ItemStack[0] : stacks;
        Set<Integer> matchedIngredients = new HashSet<>();
        for (ItemStack rawStack : safeStacks) {
            ItemStack stack = rawStack == null ? ItemStack.EMPTY : rawStack;
            if (stack.isEmpty()) {
                continue;
            }
            boolean matched = false;
            for (int i = 0; i < safeIngredients.size(); i++) {
                HbmIngredient ingredient = safeIngredients.get(i);
                if (!matchedIngredients.contains(i) && ingredient.test(stack)) {
                    matchedIngredients.add(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return matchedIngredients.size() == safeIngredients.size();
    }

    public static boolean isTopping(Level level, ItemStack stack) {
        return !stack.isEmpty() && recipes(level).stream()
                .flatMap(recipe -> recipe.toppings().stream())
                .anyMatch(input -> input.test(stack, true));
    }

    public static boolean isPcb(Level level, ItemStack stack) {
        return !stack.isEmpty() && recipes(level).stream()
                .flatMap(recipe -> recipe.pcb().stream())
                .anyMatch(input -> input.test(stack, true));
    }

    public static boolean isSolder(Level level, ItemStack stack) {
        return !stack.isEmpty() && recipes(level).stream()
                .flatMap(recipe -> recipe.solder().stream())
                .anyMatch(input -> input.test(stack, true));
    }

    public static List<SolderingStationRecipe> recipes(Level level) {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.SOLDERING_STATION.type().get()).stream()
                .sorted(Comparator.comparingInt(SolderingStationRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }
}
