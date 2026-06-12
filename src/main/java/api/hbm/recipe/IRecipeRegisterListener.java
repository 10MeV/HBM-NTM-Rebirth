package api.hbm.recipe;

/**
 * Legacy 1.7.10 package bridge for addon recipe registration callbacks.
 *
 * <p>The modern contract emits datapack-compatible JSON through
 * {@link IRecipeSink}; it intentionally does not expose the old runtime
 * SerializableRecipe map mutation hook.</p>
 */
@Deprecated(forRemoval = false)
@FunctionalInterface
public interface IRecipeRegisterListener extends com.hbm.ntm.api.recipe.IRecipeRegisterListener {
}
