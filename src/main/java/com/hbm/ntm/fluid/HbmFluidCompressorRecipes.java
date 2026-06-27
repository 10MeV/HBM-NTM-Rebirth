package com.hbm.ntm.fluid;

import com.hbm.ntm.recipe.CompressorRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class HbmFluidCompressorRecipes {
    private static final List<RecipeEntry> DEFAULT_RECIPES = List.of(
            defaultRecipe(HbmFluids.PETROLEUM, 0, 2_000, HbmFluids.PETROLEUM, 2_000, 1, 20),
            defaultRecipe(HbmFluids.PETROLEUM, 1, 2_000, HbmFluids.LPG, 1_000, 0, 20),
            defaultRecipe(HbmFluids.BLOOD, 3, 1_000, HbmFluids.HEAVYOIL, 250, 0, 200),
            defaultRecipe(HbmFluids.PERFLUOROMETHYL, 0, 1_000, HbmFluids.PERFLUOROMETHYL, 1_000, 1, 50),
            defaultRecipe(HbmFluids.PERFLUOROMETHYL, 1, 1_000, HbmFluids.PERFLUOROMETHYL_COLD, 1_000, 0, 50));
    private static final Map<Key, Recipe> RUNTIME_RECIPES = new LinkedHashMap<>();

    public static void register(FluidType inputType, int inputPressure, int inputAmount,
            FluidType outputType, int outputAmount, int outputPressure, int duration) {
        if (inputType == null || inputType == HbmFluids.NONE) {
            throw new IllegalArgumentException("Compressor input fluid cannot be empty");
        }
        if (outputType == null || outputType == HbmFluids.NONE) {
            throw new IllegalArgumentException("Compressor output fluid cannot be empty");
        }
        RUNTIME_RECIPES.put(new Key(inputType, HbmFluidTank.clampPressure(inputPressure)),
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
        RUNTIME_RECIPES.put(new Key(input.type(), input.pressure()), recipe);
        return recipe;
    }

    public static @Nullable Recipe find(FluidType inputType, int inputPressure) {
        Recipe recipe = RUNTIME_RECIPES.get(new Key(inputType, HbmFluidTank.clampPressure(inputPressure)));
        return recipe != null ? recipe : findDefault(inputType, inputPressure);
    }

    public static @Nullable Recipe find(@Nullable Level level, FluidType inputType, int inputPressure) {
        Key key = new Key(inputType, HbmFluidTank.clampPressure(inputPressure));
        if (level != null) {
            List<CompressorRecipe> datapackRecipes =
                    level.getRecipeManager().getAllRecipesFor(ModRecipes.COMPRESSOR.type().get());
            Recipe recipe = findDatapack(datapackRecipes, key);
            if (recipe != null) {
                return recipe;
            }
            recipe = RUNTIME_RECIPES.get(key);
            if (recipe != null || !datapackRecipes.isEmpty()) {
                return recipe;
            }
        }
        return find(inputType, inputPressure);
    }

    public static HbmFluidStack outputFor(FluidType inputType, int inputPressure) {
        Recipe recipe = find(inputType, inputPressure);
        if (recipe != null) {
            return recipe.output();
        }
        int nextPressure = HbmFluidTank.clampPressure(inputPressure + 1);
        return new HbmFluidStack(inputType, 1_000, nextPressure);
    }

    public static HbmFluidStack outputFor(@Nullable Level level, FluidType inputType, int inputPressure) {
        Recipe recipe = find(level, inputType, inputPressure);
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

    public static int durationFor(@Nullable Level level, FluidType inputType, int inputPressure) {
        Recipe recipe = find(level, inputType, inputPressure);
        return recipe == null ? 100 : recipe.duration();
    }

    public static int inputAmountFor(FluidType inputType, int inputPressure) {
        Recipe recipe = find(inputType, inputPressure);
        return recipe == null ? 1_000 : recipe.inputAmount();
    }

    public static int inputAmountFor(@Nullable Level level, FluidType inputType, int inputPressure) {
        Recipe recipe = find(level, inputType, inputPressure);
        return recipe == null ? 1_000 : recipe.inputAmount();
    }

    public static List<RecipeEntry> recipes() {
        return recipes(null);
    }

    public static List<RecipeEntry> recipes(@Nullable RecipeManager recipeManager) {
        Map<Key, RecipeEntry> entries = new LinkedHashMap<>();
        if (recipeManager != null) {
            for (CompressorRecipe recipe : recipeManager.getAllRecipesFor(ModRecipes.COMPRESSOR.type().get())) {
                RecipeEntry entry = fromDatapack(recipe);
                entries.put(new Key(entry.inputType(), entry.inputPressure()), entry);
            }
        }
        for (Map.Entry<Key, Recipe> entry : RUNTIME_RECIPES.entrySet()) {
            entries.putIfAbsent(entry.getKey(),
                    new RecipeEntry(entry.getKey().type(), entry.getKey().pressure(), entry.getValue()));
        }
        if (entries.isEmpty()) {
            for (RecipeEntry entry : DEFAULT_RECIPES) {
                entries.put(new Key(entry.inputType(), entry.inputPressure()), entry);
            }
        }
        return new ArrayList<>(entries.values());
    }

    private static RecipeEntry defaultRecipe(FluidType inputType, int inputPressure, int inputAmount,
            FluidType outputType, int outputAmount, int outputPressure, int duration) {
        return new RecipeEntry(inputType, HbmFluidTank.clampPressure(inputPressure),
                new Recipe(inputAmount, new HbmFluidStack(outputType, outputAmount, outputPressure), duration));
    }

    private static @Nullable Recipe findDefault(FluidType inputType, int inputPressure) {
        Key key = new Key(inputType, HbmFluidTank.clampPressure(inputPressure));
        for (RecipeEntry entry : DEFAULT_RECIPES) {
            if (new Key(entry.inputType(), entry.inputPressure()).equals(key)) {
                return entry.recipe();
            }
        }
        return null;
    }

    private static @Nullable Recipe findDatapack(List<CompressorRecipe> recipes, Key key) {
        for (CompressorRecipe recipe : recipes) {
            RecipeEntry entry = fromDatapack(recipe);
            if (new Key(entry.inputType(), entry.inputPressure()).equals(key)) {
                return entry.recipe();
            }
        }
        return null;
    }

    private static RecipeEntry fromDatapack(CompressorRecipe recipe) {
        HbmFluidStack input = recipe.input();
        return new RecipeEntry(input.type(), input.pressure(),
                new Recipe(input.amount(), recipe.output(), recipe.duration()));
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
