package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

public final class RadiolysisRecipes {
    private static final Map<FluidType, Result> RECIPES = new LinkedHashMap<>();

    static {
        register(HbmFluids.WATER,
                new HbmFluidStack(HbmFluids.PEROXIDE, 80, 0),
                new HbmFluidStack(HbmFluids.HYDROGEN, 20, 0));
        for (Map.Entry<FluidType, LegacyOilFluidRecipes.PairRecipe> entry : LegacyOilFluidRecipes.crackingRecipes()) {
            register(entry.getKey(), entry.getValue().left(), entry.getValue().right());
        }
    }

    private RadiolysisRecipes() {
    }

    public static void register(FluidType input, HbmFluidStack left, HbmFluidStack right) {
        if (input != null && input != HbmFluids.NONE && left != null && right != null) {
            RECIPES.put(input, new Result(left, right));
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
            return RECIPES;
        }
        List<Map.Entry<FluidType, LegacyOilFluidRecipes.PairRecipe>> cracking =
                LegacyOilFluidRecipes.crackingRecipes(recipeManager);
        if (cracking.isEmpty()) {
            return RECIPES;
        }
        Map<FluidType, Result> recipes = new LinkedHashMap<>();
        Result water = RECIPES.get(HbmFluids.WATER);
        if (water != null) {
            recipes.put(HbmFluids.WATER, water);
        }
        for (Map.Entry<FluidType, LegacyOilFluidRecipes.PairRecipe> entry : cracking) {
            LegacyOilFluidRecipes.PairRecipe pair = entry.getValue();
            if (entry.getKey() != null && pair != null) {
                recipes.put(entry.getKey(), new Result(pair.left(), pair.right()));
            }
        }
        return Collections.unmodifiableMap(recipes);
    }

    public record DisplayRecipe(HbmFluidStack input, HbmFluidStack left, HbmFluidStack right) {
    }

    public record Result(HbmFluidStack left, HbmFluidStack right) {
    }
}
