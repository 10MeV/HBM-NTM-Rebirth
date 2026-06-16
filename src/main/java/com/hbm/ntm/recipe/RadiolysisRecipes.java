package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class RadiolysisRecipes {
    private static final Map<FluidType, Result> RECIPES = new LinkedHashMap<>();

    static {
        register(HbmFluids.WATER,
                new HbmFluidStack(HbmFluids.PEROXIDE, 80, 0),
                new HbmFluidStack(HbmFluids.HYDROGEN, 20, 0));
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

    public record Result(HbmFluidStack left, HbmFluidStack right) {
    }
}
