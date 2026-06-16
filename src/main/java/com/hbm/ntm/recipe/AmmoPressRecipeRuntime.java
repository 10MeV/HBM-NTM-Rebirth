package com.hbm.ntm.recipe;

import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;

public final class AmmoPressRecipeRuntime {
    private static final Comparator<AmmoPressRecipe> ORDER = Comparator
            .comparingInt(AmmoPressRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.getId().toString());

    public static List<AmmoPressRecipe> recipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.AMMO_PRESS.type().get()).stream()
                .sorted(ORDER)
                .toList();
    }

    private AmmoPressRecipeRuntime() {
    }
}
