package com.hbm.ntm.api.recipe;

@FunctionalInterface
public interface RecipeRegisterListener {
    void registerRecipes(RecipeSink sink);
}
