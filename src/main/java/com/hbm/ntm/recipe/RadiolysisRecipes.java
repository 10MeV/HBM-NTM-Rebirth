package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        return RECIPES.get(input);
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (Map.Entry<FluidType, Result> entry : RECIPES.entrySet()) {
            recipes.add(new DisplayRecipe(new HbmFluidStack(entry.getKey(), 100, 0),
                    entry.getValue().left(), entry.getValue().right()));
        }
        return List.copyOf(recipes);
    }

    public record DisplayRecipe(HbmFluidStack input, HbmFluidStack left, HbmFluidStack right) {
    }

    public record Result(HbmFluidStack left, HbmFluidStack right) {
    }
}
