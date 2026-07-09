package com.hbm.ntm.recipe;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LemegetonRecipeRuntime {
    private static final Comparator<LemegetonRecipe> ORDER = Comparator
            .comparingInt(LemegetonRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.getId().toString());

    private LemegetonRecipeRuntime() {
    }

    public static List<LemegetonRecipe> recipes(Level level) {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.LEMEGETON.type().get()).stream()
                .sorted(ORDER)
                .toList();
    }

    public static Optional<LemegetonRecipe> find(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        return recipes(level).stream()
                .filter(recipe -> recipe.matches(input))
                .findFirst();
    }

    public static ItemStack result(Level level, ItemStack input) {
        return find(level, input)
                .map(LemegetonRecipe::result)
                .orElse(ItemStack.EMPTY);
    }

    public static boolean isIngredient(Level level, ItemStack input) {
        return !input.isEmpty() && recipes(level).stream().anyMatch(recipe -> recipe.matches(input));
    }
}
