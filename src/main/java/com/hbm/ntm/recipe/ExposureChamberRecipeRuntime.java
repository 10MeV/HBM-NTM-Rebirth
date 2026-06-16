package com.hbm.ntm.recipe;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ExposureChamberRecipeRuntime {
    private ExposureChamberRecipeRuntime() {
    }

    public static ExposureChamberRecipe find(Level level, ItemStack particle, ItemStack ingredient) {
        if (level == null || particle.isEmpty() || ingredient.isEmpty()) {
            return null;
        }
        return recipes(level).stream()
                .filter(recipe -> recipe.matches(particle, ingredient))
                .findFirst()
                .orElse(null);
    }

    public static boolean isParticle(Level level, ItemStack stack) {
        return !stack.isEmpty() && recipes(level).stream().anyMatch(recipe -> recipe.particle().test(stack, true));
    }

    public static boolean isIngredient(Level level, ItemStack stack) {
        return !stack.isEmpty() && recipes(level).stream().anyMatch(recipe -> recipe.ingredient().test(stack, true));
    }

    public static List<ExposureChamberRecipe> recipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.EXPOSURE_CHAMBER.type().get()).stream()
                .sorted(Comparator.comparingInt(ExposureChamberRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }
}
