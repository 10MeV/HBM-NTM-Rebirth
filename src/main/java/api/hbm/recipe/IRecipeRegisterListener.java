package api.hbm.recipe;

/**
 * Legacy 1.7.10 package bridge for addon recipe registration callbacks.
 */
@Deprecated(forRemoval = false)
@FunctionalInterface
public interface IRecipeRegisterListener {
    /**
     * Called by the legacy SerializableRecipe lifecycle after one recipe
     * handler registers its defaults and before the old template is written.
     *
     * <p>The modern port does not restore that config-directory lifecycle, but
     * keeps this callback shape so old addon/source migrations compile against
     * the documented 1.7.10 API.</p>
     */
    void onRecipeLoad(String recipeClassName);
}
