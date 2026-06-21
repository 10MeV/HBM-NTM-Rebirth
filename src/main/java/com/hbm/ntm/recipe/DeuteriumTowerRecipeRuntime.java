package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.List;

public final class DeuteriumTowerRecipeRuntime {
    public static final int WATER_PER_HEAVY_WATER = 50;
    public static final long POWER_PER_OPERATION = 5_000L;

    private static final DisplayRecipe DISPLAY = new DisplayRecipe(
            new HbmFluidStack(HbmFluids.WATER, 1_000, 0),
            new HbmFluidStack(HbmFluids.HEAVYWATER, 20, 0),
            POWER_PER_OPERATION);

    private DeuteriumTowerRecipeRuntime() {
    }

    public static List<DisplayRecipe> displayRecipes() {
        return List.of(DISPLAY);
    }

    public record DisplayRecipe(HbmFluidStack input, HbmFluidStack output, long powerPerOperation) {
    }
}