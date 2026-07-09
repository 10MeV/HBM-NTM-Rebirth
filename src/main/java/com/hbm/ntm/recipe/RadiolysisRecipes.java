package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

public final class RadiolysisRecipes {
    private static final Map<FluidType, Result> FALLBACK_RECIPES = new LinkedHashMap<>();

    static {
        registerFallback(HbmFluids.WATER,
                new HbmFluidStack(HbmFluids.PEROXIDE, 80, 0),
                new HbmFluidStack(HbmFluids.HYDROGEN, 20, 0));
        for (Map.Entry<FluidType, LegacyOilFluidRecipes.PairRecipe> entry : LegacyOilFluidRecipes.crackingRecipes()) {
            registerFallback(entry.getKey(), entry.getValue().left(), entry.getValue().right());
        }
    }

    private RadiolysisRecipes() {
    }

    private static void registerFallback(FluidType input, HbmFluidStack left, HbmFluidStack right) {
        if (input != null && input != HbmFluids.NONE && left != null && right != null) {
            FALLBACK_RECIPES.put(input, new Result(left, right));
        }
    }

    @Nullable
    public static Result getRadiolysis(FluidType input) {
        return recipes(null).get(input);
    }

    @Nullable
    public static Result getRadiolysis(@Nullable RecipeManager recipeManager, FluidType input) {
        return recipes(recipeManager).get(input);
    }

    public static List<DisplayRecipe> displayRecipes() {
        return displayRecipes(null);
    }

    public static List<DisplayRecipe> displayRecipes(@Nullable RecipeManager recipeManager) {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (Map.Entry<FluidType, Result> entry : recipes(recipeManager).entrySet()) {
            recipes.add(new DisplayRecipe(new HbmFluidStack(entry.getKey(), 100, 0),
                    entry.getValue().left(), entry.getValue().right()));
        }
        return List.copyOf(recipes);
    }

    private static Map<FluidType, Result> recipes(@Nullable RecipeManager recipeManager) {
        if (recipeManager == null) {
            return FALLBACK_RECIPES;
        }
        Map<FluidType, Result> recipes = new LinkedHashMap<>();
        recipeManager.getAllRecipesFor(ModRecipes.RADIOLYSIS.type().get()).stream()
                .sorted(Comparator.comparingInt(RadiolysisRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .forEach(recipe -> recipes.put(recipe.input().type(),
                        new Result(recipe.output1(), recipe.output2())));
        List<Map.Entry<FluidType, LegacyOilFluidRecipes.PairRecipe>> cracking =
                LegacyOilFluidRecipes.crackingRecipes(recipeManager);
        for (Map.Entry<FluidType, LegacyOilFluidRecipes.PairRecipe> entry : cracking) {
            LegacyOilFluidRecipes.PairRecipe pair = entry.getValue();
            if (entry.getKey() != null && pair != null) {
                recipes.putIfAbsent(entry.getKey(), new Result(pair.left(), pair.right()));
            }
        }
        return Collections.unmodifiableMap(recipes);
    }

    public record DisplayRecipe(HbmFluidStack input, HbmFluidStack left, HbmFluidStack right) {
    }

    public record Result(HbmFluidStack left, HbmFluidStack right) {
    }
}
