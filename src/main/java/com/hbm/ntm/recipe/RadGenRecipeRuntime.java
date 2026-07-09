package com.hbm.ntm.recipe;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class RadGenRecipeRuntime {
    private RadGenRecipeRuntime() {
    }

    public static List<RadGenRecipe> displayRecipes(RecipeManager recipeManager) {
        return recipeManager.getAllRecipesFor(ModRecipes.RADGEN.type().get()).stream()
                .sorted(Comparator.comparingInt(RadGenRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    @Nullable
    public static FuelSpec fuelFor(@Nullable Level level, ItemStack stack) {
        if (level == null || stack == null || stack.isEmpty()) {
            return null;
        }
        return displayRecipes(level.getRecipeManager()).stream()
                .filter(recipe -> recipe.matches(stack))
                .findFirst()
                .map(FuelSpec::fromRecipe)
                .orElse(null);
    }

    public record FuelSpec(int powerPerTick, int duration, ItemStack output) {
        public FuelSpec {
            output = output == null ? ItemStack.EMPTY : output.copy();
        }

        private static FuelSpec fromRecipe(RadGenRecipe recipe) {
            return new FuelSpec(recipe.powerPerTick(), recipe.duration(), recipe.output());
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }
    }
}
