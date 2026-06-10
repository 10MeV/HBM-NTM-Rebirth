package com.hbm.ntm.api.recipe;

/**
 * Legacy-name bridge for addon recipe registration callbacks.
 *
 * <p>The 1.7.10 listener received a SerializableRecipe class name and mutated
 * static recipe maps. The modern bridge emits datapack-compatible JSON through
 * {@link RecipeSink}; it intentionally does not expose runtime map mutation.</p>
 */
@Deprecated(forRemoval = false)
@FunctionalInterface
public interface IRecipeRegisterListener extends RecipeRegisterListener {
}
