package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.CokerRecipe;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.PairRecipe;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.RefineryRecipe;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.SolidificationRecipe;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.TripleRecipe;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.VacuumRecipe;
import com.hbm.ntm.recipe.LiquefactionRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record HbmOilRecipe(
        List<Ingredient> itemInputs,
        List<HbmFluidStack> fluidInputs,
        List<ItemStack> itemOutputs,
        List<HbmFluidStack> fluidOutputs) {

    public static HbmOilRecipe cracking(FluidType input, PairRecipe recipe) {
        return fluid(List.of(stack(input, 100), stack(com.hbm.ntm.fluid.HbmFluids.STEAM, 200)),
                List.of(recipe.left(), recipe.right(), stack(com.hbm.ntm.fluid.HbmFluids.SPENTSTEAM, 2)));
    }

    public static HbmOilRecipe fractioning(FluidType input, PairRecipe recipe) {
        return fluid(List.of(stack(input, 100)), List.of(recipe.left(), recipe.right()));
    }

    public static HbmOilRecipe hydrotreating(FluidType input, TripleRecipe recipe) {
        return fluid(List.of(stack(input, 100), recipe.first()), List.of(recipe.second(), recipe.third()));
    }

    public static HbmOilRecipe reforming(FluidType input, TripleRecipe recipe) {
        return fluid(List.of(stack(input, 100)), List.of(recipe.first(), recipe.second(), recipe.third()));
    }

    public static HbmOilRecipe vacuum(FluidType input, VacuumRecipe recipe) {
        return fluid(List.of(stack(input, 100)), List.of(recipe.outputs()));
    }

    public static HbmOilRecipe refinery(FluidType input, RefineryRecipe recipe) {
        List<ItemStack> itemOutputs = new ArrayList<>();
        ItemStack solid = recipe.solidStack();
        if (!solid.isEmpty()) {
            itemOutputs.add(solid);
        }
        return new HbmOilRecipe(List.of(), List.of(stack(input, 100)), itemOutputs, List.of(recipe.outputs()));
    }

    public static HbmOilRecipe solidification(FluidType input, SolidificationRecipe recipe) {
        return new HbmOilRecipe(List.of(), List.of(stack(input, recipe.inputAmount())),
                List.of(recipe.outputStack()), List.of());
    }

    public static HbmOilRecipe coking(FluidType input, CokerRecipe recipe) {
        List<HbmFluidStack> outputs = recipe.byproduct() == null ? List.of() : List.of(recipe.byproduct());
        return new HbmOilRecipe(List.of(), List.of(stack(input, recipe.inputAmount())),
                List.of(recipe.outputStack()), outputs);
    }

    public static HbmOilRecipe liquefaction(LiquefactionRecipe recipe) {
        return new HbmOilRecipe(List.of(recipe.getIngredients().get(0)), List.of(), List.of(),
                List.of(recipe.getOutputFluid()));
    }

    public static List<HbmOilRecipe> crackingRecipes() {
        return LegacyOilFluidRecipes.crackingRecipes().stream()
                .map(entry -> cracking(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<HbmOilRecipe> fractioningRecipes() {
        return LegacyOilFluidRecipes.fractioningRecipes().stream()
                .map(entry -> fractioning(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<HbmOilRecipe> hydrotreatingRecipes() {
        return LegacyOilFluidRecipes.hydrotreatingRecipes().stream()
                .map(entry -> hydrotreating(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<HbmOilRecipe> reformingRecipes() {
        return LegacyOilFluidRecipes.reformingRecipes().stream()
                .map(entry -> reforming(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<HbmOilRecipe> vacuumRecipes() {
        return LegacyOilFluidRecipes.vacuumRecipes().stream()
                .map(entry -> vacuum(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<HbmOilRecipe> refineryRecipes() {
        return LegacyOilFluidRecipes.refineryRecipes().stream()
                .map(entry -> refinery(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<HbmOilRecipe> solidificationRecipes() {
        return LegacyOilFluidRecipes.solidificationRecipes().stream()
                .map(entry -> solidification(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static List<HbmOilRecipe> cokingRecipes() {
        return LegacyOilFluidRecipes.cokingRecipes().stream()
                .map(entry -> coking(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static HbmOilRecipe fluid(List<HbmFluidStack> inputs, List<HbmFluidStack> outputs) {
        return new HbmOilRecipe(List.of(), inputs, List.of(), outputs);
    }

    private static HbmFluidStack stack(FluidType type, int amount) {
        return new HbmFluidStack(type, amount);
    }
}
