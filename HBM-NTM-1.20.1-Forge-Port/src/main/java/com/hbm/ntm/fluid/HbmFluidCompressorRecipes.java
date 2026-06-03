package com.hbm.ntm.fluid;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class HbmFluidCompressorRecipes {
    private static final Map<Key, Recipe> RECIPES = new LinkedHashMap<>();

    static {
        register(HbmFluids.PETROLEUM, 0, 2_000, HbmFluids.PETROLEUM, 2_000, 1, 20);
        register(HbmFluids.PETROLEUM, 1, 2_000, HbmFluids.LPG, 1_000, 0, 20);
        register(HbmFluids.BLOOD, 3, 1_000, HbmFluids.HEAVYOIL, 250, 0, 200);
        register(HbmFluids.PERFLUOROMETHYL, 0, 1_000, HbmFluids.PERFLUOROMETHYL, 1_000, 1, 50);
        register(HbmFluids.PERFLUOROMETHYL, 1, 1_000, HbmFluids.PERFLUOROMETHYL_COLD, 1_000, 0, 50);
    }

    public static void register(FluidType inputType, int inputPressure, int inputAmount,
            FluidType outputType, int outputAmount, int outputPressure, int duration) {
        RECIPES.put(new Key(inputType, HbmFluidTank.clampPressure(inputPressure)),
                new Recipe(inputAmount, new HbmFluidStack(outputType, outputAmount, outputPressure),
                        Math.max(1, duration)));
    }

    public static @Nullable Recipe find(FluidType inputType, int inputPressure) {
        return RECIPES.get(new Key(inputType, HbmFluidTank.clampPressure(inputPressure)));
    }

    public static HbmFluidStack outputFor(FluidType inputType, int inputPressure) {
        Recipe recipe = find(inputType, inputPressure);
        if (recipe != null) {
            return recipe.output();
        }
        int nextPressure = HbmFluidTank.clampPressure(inputPressure + 1);
        return new HbmFluidStack(inputType, 1_000, nextPressure);
    }

    public static int durationFor(FluidType inputType, int inputPressure) {
        Recipe recipe = find(inputType, inputPressure);
        return recipe == null ? 100 : recipe.duration();
    }

    public static int inputAmountFor(FluidType inputType, int inputPressure) {
        Recipe recipe = find(inputType, inputPressure);
        return recipe == null ? 1_000 : recipe.inputAmount();
    }

    public record Recipe(int inputAmount, HbmFluidStack output, int duration) {
        public Recipe {
            inputAmount = Math.max(1, inputAmount);
            duration = Math.max(1, duration);
        }
    }

    private record Key(FluidType type, int pressure) {
    }

    private HbmFluidCompressorRecipes() {
    }
}
