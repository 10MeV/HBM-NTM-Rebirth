package com.hbm.ntm.api.recipe;

/**
 * Legacy-name bridge for addon recipe registration callbacks.
 *
 * <p>The 1.7.10 listener receives a SerializableRecipe class name. Modern
 * datapack-compatible recipe emission uses {@link RecipeRegisterListener}
 * instead.</p>
 */
@Deprecated(forRemoval = false)
@FunctionalInterface
public interface IRecipeRegisterListener extends api.hbm.recipe.IRecipeRegisterListener {
}
