package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.level.Level;

public final class MixerRecipeRuntime {
    private MixerRecipeRuntime() {
    }

    public static List<MixerRecipe> recipesForOutput(Level level, FluidType output) {
        if (level == null || output == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.MIXER.type().get()).stream()
                .filter(recipe -> recipe.output().type() == output)
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    public static MixerRecipe recipeForOutput(Level level, FluidType output, int index) {
        List<MixerRecipe> recipes = recipesForOutput(level, output);
        if (recipes.isEmpty()) {
            return null;
        }
        int selected = Math.floorMod(index, recipes.size());
        return recipes.get(selected);
    }
}
