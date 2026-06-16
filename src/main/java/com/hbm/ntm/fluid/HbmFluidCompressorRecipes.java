package com.hbm.ntm.fluid;

import java.util.LinkedHashMap;
import java.util.List;
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
        if (inputType == null || inputType == HbmFluids.NONE) {
            throw new IllegalArgumentException("Compressor input fluid cannot be empty");
        }
        if (outputType == null || outputType == HbmFluids.NONE) {
            throw new IllegalArgumentException("Compressor output fluid cannot be empty");
        }
        RECIPES.put(new Key(inputType, HbmFluidTank.clampPressure(inputPressure)),
                new Recipe(inputAmount, new HbmFluidStack(outputType, outputAmount, outputPressure),
                        Math.max(1, duration)));
    }

    public static Recipe register(HbmFluidStack input, HbmFluidStack output, int duration) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Compressor input fluid stack cannot be empty");
        }
        if (output == null || output.isEmpty()) {
            throw new IllegalArgumentException("Compressor output fluid stack cannot be empty");
        }
        Recipe recipe = new Recipe(input.amount(), output, duration);
        RECIPES.put(new Key(input.type(), input.pressure()), recipe);
        return recipe;
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

    public static List<RecipeEntry> recipes() {
        return RECIPES.entrySet().stream()
                .map(entry -> new RecipeEntry(entry.getKey().type(), entry.getKey().pressure(), entry.getValue()))
                .toList();
    }

    public record Recipe(int inputAmount, HbmFluidStack output, int duration) {
        public Recipe {
            inputAmount = Math.max(1, inputAmount);
            duration = Math.max(1, duration);
        }
    }

    public record RecipeEntry(FluidType inputType, int inputPressure, Recipe recipe) {
    }

    private record Key(FluidType type, int pressure) {
    }

    private HbmFluidCompressorRecipes() {
    }
}
