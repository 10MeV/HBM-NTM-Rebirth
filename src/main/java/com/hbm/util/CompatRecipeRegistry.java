package com.hbm.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NBTStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.recipe.PedestalRecipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Legacy package facade for supported modern compat recipe JSON emitters.
 */
@Deprecated(forRemoval = false)
public final class CompatRecipeRegistry {
    private static final AtomicInteger LEGACY_DIRECT_RECIPE_COUNTER = new AtomicInteger();

    public static void registerRecipeRegisterListener(com.hbm.ntm.api.recipe.RecipeRegisterListener listener) {
        com.hbm.ntm.compat.CompatRecipeRegistry.registerRecipeRegisterListener(listener);
    }

    public static boolean unregisterRecipeRegisterListener(com.hbm.ntm.api.recipe.RecipeRegisterListener listener) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.unregisterRecipeRegisterListener(listener);
    }

    public static void emitRecipeRegisterListeners(com.hbm.ntm.api.recipe.RecipeSink sink) {
        com.hbm.ntm.compat.CompatRecipeRegistry.emitRecipeRegisterListeners(sink);
    }

    public static JsonObject createAssembler(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack inputFluid,
            HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createAssembler(id, name, named, icon, duration, power,
                inputItems, inputFluid, outputItems, outputFluid);
    }

    public static ResourceLocation registerAssembler(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack inputFluid, HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerAssembler(sink, id, name, named, icon, duration,
                power, inputItems, inputFluid, outputItems, outputFluid);
    }

    public static ResourceLocation registerAssembler(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack inputFluid, HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerAssembler(sink, name, named, icon, duration, power,
                inputItems, inputFluid, outputItems, outputFluid);
    }

    public static JsonObject createChemicalPlant(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack[] inputFluids,
            HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createChemicalPlant(id, name, named, icon, duration, power,
                inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerChemicalPlant(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, String name, boolean named, ItemStack icon, int duration, long power,
            HbmIngredient[] inputItems, HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems,
            HbmFluidStack[] outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerChemicalPlant(sink, id, name, named, icon,
                duration, power, inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerChemicalPlant(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerChemicalPlant(sink, name, named, icon, duration,
                power, inputItems, inputFluids, outputItems, outputFluids);
    }

    public static JsonObject createPurex(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack[] inputFluids,
            HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPurex(id, name, named, icon, duration, power,
                inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerPurex(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPurex(sink, id, name, named, icon, duration, power,
                inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerPurex(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPurex(sink, name, named, icon, duration, power,
                inputItems, inputFluids, outputItems, outputFluids);
    }

    public static JsonObject createPrecass(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack inputFluid,
            HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPrecass(id, name, named, icon, duration, power,
                inputItems, inputFluid, outputItems, outputFluid);
    }

    public static ResourceLocation registerPrecass(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack inputFluid, HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPrecass(sink, id, name, named, icon, duration,
                power, inputItems, inputFluid, outputItems, outputFluid);
    }

    public static ResourceLocation registerPrecass(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack inputFluid, HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPrecass(sink, name, named, icon, duration, power,
                inputItems, inputFluid, outputItems, outputFluid);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, Ingredient input, ItemStack output,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output, sourceOrder);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, ItemLike input, ItemStack output,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output, sourceOrder);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, name, stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, name, stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, name, stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, name, stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, name, stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, name, stamp, input, output, sourceOrder);
    }

    public static JsonObject createLiquefaction(Ingredient input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output);
    }

    public static JsonObject createLiquefaction(Ingredient input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output, sourceOrder);
    }

    public static JsonObject createLiquefaction(ItemLike input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output);
    }

    public static JsonObject createLiquefaction(ItemLike input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output, sourceOrder);
    }

    public static JsonObject createLiquefaction(TagKey<Item> input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output);
    }

    public static JsonObject createLiquefaction(TagKey<Item> input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            Ingredient input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            Ingredient input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, input, output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            Ingredient input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, name, input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            Ingredient input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, name, input, output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, input, output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, input, output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, name, input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, name, input, output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, name, input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, HbmFluidStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, name, input, output, sourceOrder);
    }

    public static JsonObject createPyro(int duration, HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPyro(duration, inputItem, inputFluid, outputItem,
                outputFluid);
    }

    public static JsonObject createPyro(int duration, HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPyro(duration, inputItem, inputFluid, outputItem,
                outputFluid, sourceOrder);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem,
            HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyro(sink, id, duration, inputItem, inputFluid,
                outputItem, outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem,
            HbmFluidStack outputFluid, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyro(sink, id, duration, inputItem, inputFluid,
                outputItem, outputFluid, sourceOrder);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyro(sink, name, duration, inputItem, inputFluid,
                outputItem, outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem, HbmFluidStack outputFluid,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyro(sink, name, duration, inputItem, inputFluid,
                outputItem, outputFluid, sourceOrder);
    }

    public static JsonObject createPyroAuto(FluidType input) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPyroAuto(input);
    }

    public static JsonObject createPyroAuto(FluidType input, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPyroAuto(input, sourceOrder);
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyroAuto(sink, id, input);
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyroAuto(sink, id, input, sourceOrder);
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, FluidType input) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyroAuto(sink, input);
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, FluidType input,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyroAuto(sink, input, sourceOrder);
    }

    public static JsonObject createMixer(HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2,
            HbmIngredient solidInput, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createMixer(output, input1, input2, solidInput, duration);
    }

    public static JsonObject createMixer(HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2,
            HbmIngredient solidInput, int duration, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createMixer(output, input1, input2, solidInput, duration,
                sourceOrder);
    }

    public static JsonObject createMixer(com.hbm.inventory.FluidStack output,
            com.hbm.inventory.FluidStack input1, com.hbm.inventory.FluidStack input2,
            HbmIngredient solidInput, int duration) {
        return createMixer(output == null ? null : output.toModern(),
                input1 == null ? null : input1.toModern(),
                input2 == null ? null : input2.toModern(), solidInput, duration);
    }

    public static JsonObject createMixer(com.hbm.inventory.FluidStack output,
            com.hbm.inventory.FluidStack input1, com.hbm.inventory.FluidStack input2,
            HbmIngredient solidInput, int duration, int sourceOrder) {
        return createMixer(output == null ? null : output.toModern(),
                input1 == null ? null : input1.toModern(),
                input2 == null ? null : input2.toModern(), solidInput, duration, sourceOrder);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerMixer(sink, id, output, input1, input2,
                solidInput, duration);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerMixer(sink, id, output, input1, input2,
                solidInput, duration, sourceOrder);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerMixer(sink, name, output, input1, input2,
                solidInput, duration);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerMixer(sink, name, output, input1, input2,
                solidInput, duration, sourceOrder);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            com.hbm.inventory.FluidStack output, com.hbm.inventory.FluidStack input1,
            com.hbm.inventory.FluidStack input2, HbmIngredient solidInput, int duration) {
        return registerMixer(sink, id, output == null ? null : output.toModern(),
                input1 == null ? null : input1.toModern(),
                input2 == null ? null : input2.toModern(), solidInput, duration);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            com.hbm.inventory.FluidStack output, com.hbm.inventory.FluidStack input1,
            com.hbm.inventory.FluidStack input2, HbmIngredient solidInput, int duration, int sourceOrder) {
        return registerMixer(sink, id, output == null ? null : output.toModern(),
                input1 == null ? null : input1.toModern(),
                input2 == null ? null : input2.toModern(), solidInput, duration, sourceOrder);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            com.hbm.inventory.FluidStack output, com.hbm.inventory.FluidStack input1,
            com.hbm.inventory.FluidStack input2, HbmIngredient solidInput, int duration) {
        return registerMixer(sink, name, output == null ? null : output.toModern(),
                input1 == null ? null : input1.toModern(),
                input2 == null ? null : input2.toModern(), solidInput, duration);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            com.hbm.inventory.FluidStack output, com.hbm.inventory.FluidStack input1,
            com.hbm.inventory.FluidStack input2, HbmIngredient solidInput, int duration, int sourceOrder) {
        return registerMixer(sink, name, output == null ? null : output.toModern(),
                input1 == null ? null : input1.toModern(),
                input2 == null ? null : input2.toModern(), solidInput, duration, sourceOrder);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity, sourceOrder);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity, sourceOrder);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity, sourceOrder);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, id, machine, input, outputs,
                fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, id, machine, input, outputs,
                fluidInput, duration, productivity, sourceOrder);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, List<HbmItemOutput> outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, name, machine, input, outputs,
                fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, List<HbmItemOutput> outputs,
            HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, name, machine, input, outputs,
                fluidInput, duration, productivity, sourceOrder);
    }

    public static JsonObject createShredder(HbmIngredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createShredder(input, output);
    }

    public static JsonObject createShredder(HbmIngredient input, HbmItemOutput output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createShredder(input, output);
    }

    public static JsonObject createShredder(ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createShredder(input, output);
    }

    public static JsonObject createShredder(TagKey<Item> input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createShredder(input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerShredder(sink, id, input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerShredder(sink, id, input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerShredder(sink, name, input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerShredder(sink, name, input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerShredder(sink, name, input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerShredder(sink, name, input, output);
    }

    public static JsonObject createCentrifuge(HbmIngredient input, ItemStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCentrifuge(input, outputs);
    }

    public static JsonObject createCentrifuge(HbmIngredient input, HbmItemOutput[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCentrifuge(input, outputs);
    }

    public static JsonObject createCentrifuge(ItemLike input, ItemStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCentrifuge(input, outputs);
    }

    public static JsonObject createCentrifuge(TagKey<Item> input, ItemStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCentrifuge(input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCentrifuge(sink, id, input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCentrifuge(sink, id, input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCentrifuge(sink, name, input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCentrifuge(sink, name, input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCentrifuge(sink, name, input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCentrifuge(sink, name, input, outputs);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static JsonObject createCrystallizer(ItemLike input, ItemStack output, int duration, float productivity,
            HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput);
    }

    public static JsonObject createCrystallizer(ItemLike input, ItemStack output, int duration, float productivity,
            HbmFluidStack fluidInput, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, id, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, id, input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, id, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, id, input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static JsonObject createBlastFurnace(List<HbmIngredient> inputs, List<HbmItemOutput> outputs,
            int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBlastFurnace(inputs, outputs, duration);
    }

    public static JsonObject createBlastFurnace(HbmIngredient[] inputs, HbmItemOutput[] outputs, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBlastFurnace(inputs, outputs, duration);
    }

    public static JsonObject createBlastFurnace(HbmIngredient[] inputs, ItemStack output, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBlastFurnace(inputs, output, duration);
    }

    public static JsonObject createBlastFurnace(HbmIngredient firstInput, HbmIngredient secondInput,
            ItemStack output, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBlastFurnace(firstInput, secondInput, output,
                duration);
    }

    public static JsonObject createBlastFurnace(HbmIngredient firstInput, HbmIngredient secondInput,
            HbmItemOutput output, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBlastFurnace(firstInput, secondInput, output,
                duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, id, inputs, outputs, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputs, HbmItemOutput[] outputs, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, id, inputs, outputs, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputs, ItemStack output, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, id, inputs, output, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient firstInput, HbmIngredient secondInput, ItemStack output, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, id, firstInput, secondInput,
                output, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, name, inputs, outputs, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputs, HbmItemOutput[] outputs, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, name, inputs, outputs, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputs, ItemStack output, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, name, inputs, output, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient firstInput, HbmIngredient secondInput, ItemStack output, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBlastFurnace(sink, name, firstInput, secondInput,
                output, duration);
    }

    public static JsonObject createDiFurnace(List<HbmIngredient> inputs, HbmItemOutput output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createDiFurnace(inputs, output);
    }

    public static JsonObject createDiFurnace(HbmIngredient[] inputs, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createDiFurnace(inputs, output);
    }

    public static JsonObject createDiFurnace(HbmIngredient firstInput, HbmIngredient secondInput,
            ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createDiFurnace(firstInput, secondInput, output);
    }

    public static ResourceLocation registerDiFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            List<HbmIngredient> inputs, HbmItemOutput output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerDiFurnace(sink, id, inputs, output);
    }

    public static ResourceLocation registerDiFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputs, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerDiFurnace(sink, id, inputs, output);
    }

    public static ResourceLocation registerDiFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient firstInput, HbmIngredient secondInput, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerDiFurnace(sink, id, firstInput, secondInput, output);
    }

    public static ResourceLocation registerDiFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            List<HbmIngredient> inputs, HbmItemOutput output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerDiFurnace(sink, name, inputs, output);
    }

    public static ResourceLocation registerDiFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputs, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerDiFurnace(sink, name, inputs, output);
    }

    public static ResourceLocation registerDiFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient firstInput, HbmIngredient secondInput, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerDiFurnace(sink, name, firstInput, secondInput,
                output);
    }

    public static JsonObject createSoldering(ItemStack output, int duration, long power, HbmFluidStack fluid,
            List<HbmIngredient> toppings, List<HbmIngredient> pcb, List<HbmIngredient> solder, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createSoldering(output, duration, power, fluid, toppings,
                pcb, solder, sourceOrder);
    }

    public static JsonObject createSoldering(ItemStack output, int duration, long power, HbmFluidStack fluid,
            HbmIngredient[] toppings, HbmIngredient[] pcb, HbmIngredient[] solder, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createSoldering(output, duration, power, fluid, toppings,
                pcb, solder, sourceOrder);
    }

    public static JsonObject createSoldering(ItemStack output, int duration, long power, HbmFluidStack fluid,
            HbmIngredient[] toppings, HbmIngredient[] pcb, HbmIngredient[] solder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createSoldering(output, duration, power, fluid, toppings,
                pcb, solder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack output, int duration, long power, HbmFluidStack fluid, List<HbmIngredient> toppings,
            List<HbmIngredient> pcb, List<HbmIngredient> solder, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSoldering(sink, id, output, duration, power, fluid,
                toppings, pcb, solder, sourceOrder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSoldering(sink, id, output, duration, power, fluid,
                toppings, pcb, solder, sourceOrder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSoldering(sink, id, output, duration, power, fluid,
                toppings, pcb, solder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack fluid, List<HbmIngredient> toppings,
            List<HbmIngredient> pcb, List<HbmIngredient> solder, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSoldering(sink, name, output, duration, power, fluid,
                toppings, pcb, solder, sourceOrder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSoldering(sink, name, output, duration, power, fluid,
                toppings, pcb, solder, sourceOrder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSoldering(sink, name, output, duration, power, fluid,
                toppings, pcb, solder);
    }

    public static JsonObject createArcWelder(String name, ItemStack output, int duration, long power,
            HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createArcWelder(name, output, duration, power, inputFluid,
                inputItems);
    }

    public static JsonObject createArcWelder(ResourceLocation id, String name, ItemStack output, int duration,
            long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createArcWelder(id, name, output, duration, power,
                inputFluid, inputItems);
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, ItemStack output, int duration, long power, HbmFluidStack inputFluid,
            HbmIngredient[] inputItems) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerArcWelder(sink, id, name, output, duration, power,
                inputFluid, inputItems);
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerArcWelder(sink, name, output, duration, power,
                inputFluid, inputItems);
    }

    public static JsonObject createCrucible(String internalName, String fallbackName, ItemStack icon,
            int frequency, MaterialStack[] input, MaterialStack[] output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrucible(internalName, fallbackName, icon,
                frequency, input, output, sourceOrder);
    }

    public static ResourceLocation registerCrucible(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String internalName, String fallbackName, ItemStack icon, int frequency, MaterialStack[] input,
            MaterialStack[] output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrucible(sink, id, internalName, fallbackName,
                icon, frequency, input, output, sourceOrder);
    }

    public static ResourceLocation registerCrucible(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            String fallbackName, ItemStack icon, int frequency, MaterialStack[] input, MaterialStack[] output,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrucible(sink, name, fallbackName, icon,
                frequency, input, output, sourceOrder);
    }

    public static JsonObject createCrucibleSmelting(HbmIngredient input, MaterialStack[] output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrucibleSmelting(input, output, sourceOrder);
    }

    public static JsonObject createCrucibleSmelting(HbmIngredient input, List<MaterialStack> output,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrucibleSmelting(input, output, sourceOrder);
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input, MaterialStack[] output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrucibleSmelting(sink, id, input, output,
                sourceOrder);
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input, List<MaterialStack> output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrucibleSmelting(sink, id, input, output,
                sourceOrder);
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, MaterialStack[] output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrucibleSmelting(sink, name, input, output,
                sourceOrder);
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, List<MaterialStack> output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrucibleSmelting(sink, name, input, output,
                sourceOrder);
    }

    public static JsonObject createArcFurnace(ResourceLocation id, String name, HbmIngredient input,
            ItemStack output, MaterialStack materialOutput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createArcFurnace(id, name, input, output, materialOutput);
    }

    public static JsonObject createArcFurnace(ResourceLocation id, String name, HbmIngredient input,
            ItemStack output, MaterialStack materialOutput, int duration, long power) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createArcFurnace(id, name, input, output, materialOutput,
                duration, power);
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, HbmIngredient input, ItemStack output, MaterialStack materialOutput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerArcFurnace(sink, id, name, input, output,
                materialOutput);
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, HbmIngredient input, ItemStack output, MaterialStack materialOutput, int duration,
            long power) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerArcFurnace(sink, id, name, input, output,
                materialOutput, duration, power);
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, MaterialStack materialOutput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerArcFurnace(sink, name, input, output,
                materialOutput);
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, MaterialStack materialOutput, int duration, long power) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerArcFurnace(sink, name, input, output, materialOutput,
                duration, power);
    }

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, List<HbmIngredient> inputItems,
            List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems, List<HbmFluidStack> outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createGeneric(machine, id, name, named, icon, duration,
                power, inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            GenericMachineRecipe.Machine machine, String name, boolean named, ItemStack icon, int duration,
            long power, List<HbmIngredient> inputItems, List<HbmFluidStack> inputFluids,
            List<HbmItemOutput> outputItems, List<HbmFluidStack> outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerGeneric(sink, id, machine, name, named, icon,
                duration, power, inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            GenericMachineRecipe.Machine machine, boolean named, ItemStack icon, int duration, long power,
            List<HbmIngredient> inputItems, List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems,
            List<HbmFluidStack> outputFluids) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerGeneric(sink, name, machine, named, icon, duration,
                power, inputItems, inputFluids, outputItems, outputFluids);
    }

    public static JsonObject createFusionReactor(String name, int duration, long power, long klystron,
            long plasma, double neutrons, HbmFluidStack[] inputFluids, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFusionReactor(name, duration, power, klystron, plasma,
                neutrons, inputFluids, outputItem, outputFluid);
    }

    public static JsonObject createFusionReactor(ResourceLocation id, String name, int duration, long power,
            long klystron, long plasma, double neutrons, HbmFluidStack[] inputFluids, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFusionReactor(id, name, duration, power, klystron,
                plasma, neutrons, inputFluids, outputItem, outputFluid);
    }

    public static ResourceLocation registerFusionReactor(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, int duration, long power, long klystron, long plasma, double neutrons,
            HbmFluidStack[] inputFluids, ItemStack outputItem, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFusionReactor(sink, id, name, duration, power,
                klystron, plasma, neutrons, inputFluids, outputItem, outputFluid);
    }

    public static ResourceLocation registerFusionReactor(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            int duration, long power, long klystron, long plasma, double neutrons, HbmFluidStack[] inputFluids,
            ItemStack outputItem, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFusionReactor(sink, name, duration, power, klystron,
                plasma, neutrons, inputFluids, outputItem, outputFluid);
    }

    public static JsonObject createElectrolyzerMetal(HbmIngredient input, MaterialStack output1,
            MaterialStack output2, ItemStack[] byproducts, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createElectrolyzerMetal(input, output1, output2, byproducts,
                duration);
    }

    public static ResourceLocation registerElectrolyzerMetal(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input, MaterialStack output1, MaterialStack output2,
            ItemStack[] byproducts, int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerElectrolyzerMetal(sink, id, input, output1,
                output2, byproducts, duration);
    }

    public static ResourceLocation registerElectrolyzerMetal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, MaterialStack output1, MaterialStack output2, ItemStack[] byproducts,
            int duration) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerElectrolyzerMetal(sink, name, input, output1,
                output2, byproducts, duration);
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, List<HbmIngredient> inputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createRotaryFurnace(output, duration, steam, fluid, inputs);
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, List<HbmIngredient> inputs, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createRotaryFurnace(output, duration, steam, fluid, inputs,
                sourceOrder);
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, HbmIngredient[] inputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createRotaryFurnace(output, duration, steam, fluid, inputs);
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, HbmIngredient[] inputs, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createRotaryFurnace(output, duration, steam, fluid, inputs,
                sourceOrder);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            List<HbmIngredient> inputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerRotaryFurnace(sink, id, output, duration, steam,
                fluid, inputs);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            List<HbmIngredient> inputs, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerRotaryFurnace(sink, id, output, duration, steam,
                fluid, inputs, sourceOrder);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            HbmIngredient[] inputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerRotaryFurnace(sink, id, output, duration, steam,
                fluid, inputs);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            HbmIngredient[] inputs, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerRotaryFurnace(sink, id, output, duration, steam,
                fluid, inputs, sourceOrder);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            MaterialStack output, int duration, int steam, HbmFluidStack fluid, HbmIngredient[] inputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerRotaryFurnace(sink, name, output, duration, steam,
                fluid, inputs);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            MaterialStack output, int duration, int steam, HbmFluidStack fluid, HbmIngredient[] inputs,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerRotaryFurnace(sink, name, output, duration, steam,
                fluid, inputs, sourceOrder);
    }

    public static JsonObject createBreeder(HbmIngredient input, ItemStack output, int flux) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBreeder(input, output, flux);
    }

    public static JsonObject createBreeder(HbmIngredient input, ItemStack output, int flux, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBreeder(input, output, flux, sourceOrder);
    }

    public static JsonObject createBreeder(ItemLike input, ItemStack output, int flux) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBreeder(input, output, flux);
    }

    public static JsonObject createBreeder(ItemLike input, ItemStack output, int flux, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createBreeder(input, output, flux, sourceOrder);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int flux) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, id, input, output, flux);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int flux, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, id, input, output, flux, sourceOrder);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int flux) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, name, input, output, flux);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int flux, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, name, input, output, flux, sourceOrder);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int flux) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, id, input, output, flux);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int flux, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, id, input, output, flux, sourceOrder);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int flux) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, name, input, output, flux);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int flux, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, name, input, output, flux, sourceOrder);
    }

    public static JsonObject createFuelPool(HbmIngredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFuelPool(input, output);
    }

    public static JsonObject createFuelPool(HbmIngredient input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFuelPool(input, output, sourceOrder);
    }

    public static JsonObject createFuelPool(ItemStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFuelPool(input, output);
    }

    public static JsonObject createFuelPool(ItemStack input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFuelPool(input, output, sourceOrder);
    }

    public static JsonObject createFuelPool(ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFuelPool(input, output);
    }

    public static JsonObject createFuelPool(ItemLike input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFuelPool(input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, id, input, output);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, id, input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, name, input, output);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, name, input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, id, input, output);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, id, input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, name, input, output);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, name, input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, id, input, output);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, id, input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, name, input, output);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, name, input, output, sourceOrder);
    }

    public static JsonObject createCracking(FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCracking(input, outputs);
    }

    public static JsonObject createCracking(HbmFluidStack input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCracking(input, outputs);
    }

    public static JsonObject createCracking(HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCracking(input, output1, output2);
    }

    public static JsonObject createCracking(FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCracking(input, modernFluidStacks(outputs));
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, id, input, outputs);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, id, input, outputs);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, id, input, output1, output2);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, id, input, modernFluidStacks(outputs));
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, name, input, outputs);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, name, input, outputs);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, name, input, output1, output2);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, name, input,
                modernFluidStacks(outputs));
    }

    public static JsonObject createFraction(FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFraction(input, outputs);
    }

    public static JsonObject createFraction(HbmFluidStack input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFraction(input, outputs);
    }

    public static JsonObject createFraction(FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createFraction(input, modernFluidStacks(outputs));
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFraction(sink, id, input, outputs);
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFraction(sink, id, input, modernFluidStacks(outputs));
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFraction(sink, name, input, outputs);
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerFraction(sink, name, input,
                modernFluidStacks(outputs));
    }

    public static JsonObject createReforming(FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createReforming(input, outputs);
    }

    public static JsonObject createReforming(HbmFluidStack input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createReforming(input, outputs);
    }

    public static JsonObject createReforming(FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createReforming(input, modernFluidStacks(outputs));
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerReforming(sink, id, input, outputs);
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerReforming(sink, id, input, modernFluidStacks(outputs));
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerReforming(sink, name, input, outputs);
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerReforming(sink, name, input,
                modernFluidStacks(outputs));
    }

    public static JsonObject createHydrotreating(FluidType input, HbmFluidStack hydrogen,
            HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createHydrotreating(input, hydrogen, outputs);
    }

    public static JsonObject createHydrotreating(HbmFluidStack input, HbmFluidStack hydrogen,
            HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createHydrotreating(input, hydrogen, outputs);
    }

    public static JsonObject createHydrotreating(FluidType input, com.hbm.inventory.FluidStack hydrogen,
            com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createHydrotreating(input,
                hydrogen == null ? null : hydrogen.toModern(), modernFluidStacks(outputs));
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, FluidType input, HbmFluidStack hydrogen, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerHydrotreating(sink, id, input, hydrogen, outputs);
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, FluidType input, com.hbm.inventory.FluidStack hydrogen,
            com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerHydrotreating(sink, id, input,
                hydrogen == null ? null : hydrogen.toModern(), modernFluidStacks(outputs));
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack hydrogen, HbmFluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerHydrotreating(sink, name, input, hydrogen, outputs);
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, com.hbm.inventory.FluidStack hydrogen, com.hbm.inventory.FluidStack[] outputs) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerHydrotreating(sink, name, input,
                hydrogen == null ? null : hydrogen.toModern(), modernFluidStacks(outputs));
    }

    public static JsonObject createSolidifying(HbmFluidStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createSolidifying(input, output);
    }

    public static JsonObject createSolidifying(com.hbm.inventory.FluidStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createSolidifying(input == null ? null : input.toModern(),
                output);
    }

    public static ResourceLocation registerSolidifying(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSolidifying(sink, id, input, output);
    }

    public static ResourceLocation registerSolidifying(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            com.hbm.inventory.FluidStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSolidifying(sink, id,
                input == null ? null : input.toModern(), output);
    }

    public static ResourceLocation registerSolidifying(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSolidifying(sink, name, input, output);
    }

    public static ResourceLocation registerSolidifying(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            com.hbm.inventory.FluidStack input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerSolidifying(sink, name,
                input == null ? null : input.toModern(), output);
    }

    public static JsonObject createCoker(HbmFluidStack input, ItemStack output, HbmFluidStack byproduct) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCoker(input, output, byproduct);
    }

    public static JsonObject createCoker(com.hbm.inventory.FluidStack input, ItemStack output,
            com.hbm.inventory.FluidStack byproduct) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCoker(input == null ? null : input.toModern(), output,
                byproduct == null ? null : byproduct.toModern());
    }

    public static ResourceLocation registerCoker(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, ItemStack output, HbmFluidStack byproduct) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCoker(sink, id, input, output, byproduct);
    }

    public static ResourceLocation registerCoker(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            com.hbm.inventory.FluidStack input, ItemStack output, com.hbm.inventory.FluidStack byproduct) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCoker(sink, id,
                input == null ? null : input.toModern(), output, byproduct == null ? null : byproduct.toModern());
    }

    public static ResourceLocation registerCoker(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, ItemStack output, HbmFluidStack byproduct) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCoker(sink, name, input, output, byproduct);
    }

    public static ResourceLocation registerCoker(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            com.hbm.inventory.FluidStack input, ItemStack output, com.hbm.inventory.FluidStack byproduct) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCoker(sink, name,
                input == null ? null : input.toModern(), output, byproduct == null ? null : byproduct.toModern());
    }

    public static JsonObject createCokerAuto(FluidType input, FluidType byproductType) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCokerAuto(input, byproductType);
    }

    public static ResourceLocation registerCokerAuto(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, FluidType byproductType) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCokerAuto(sink, id, input, byproductType);
    }

    public static ResourceLocation registerCokerAuto(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, FluidType byproductType) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCokerAuto(sink, name, input, byproductType);
    }

    public static JsonObject createExposureChamber(HbmIngredient particle, HbmIngredient ingredient,
            ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createExposureChamber(particle, ingredient, output);
    }

    public static JsonObject createExposureChamber(HbmIngredient particle, HbmIngredient ingredient,
            ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createExposureChamber(particle, ingredient, output,
                sourceOrder);
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient particle, HbmIngredient ingredient, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerExposureChamber(sink, id, particle, ingredient,
                output);
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient particle, HbmIngredient ingredient, ItemStack output,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerExposureChamber(sink, id, particle, ingredient,
                output, sourceOrder);
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient particle, HbmIngredient ingredient, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerExposureChamber(sink, name, particle, ingredient,
                output);
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient particle, HbmIngredient ingredient, ItemStack output, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerExposureChamber(sink, name, particle, ingredient,
                output, sourceOrder);
    }

    public static JsonObject createCyclotron(HbmIngredient particle, HbmIngredient input, ItemStack output,
            int antimatter, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCyclotron(particle, input, output, antimatter,
                sourceOrder);
    }

    public static ResourceLocation registerCyclotron(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient particle, HbmIngredient input, ItemStack output, int antimatter, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCyclotron(sink, id, particle, input, output,
                antimatter, sourceOrder);
    }

    public static ResourceLocation registerCyclotron(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient particle, HbmIngredient input, ItemStack output, int antimatter, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCyclotron(sink, name, particle, input, output,
                antimatter, sourceOrder);
    }

    public static JsonObject createParticleAccelerator(HbmIngredient input1, HbmIngredient input2, int momentum,
            ItemStack output1, ItemStack output2) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createParticleAccelerator(input1, input2, momentum, output1,
                output2);
    }

    public static JsonObject createParticleAccelerator(HbmIngredient input1, HbmIngredient input2, int momentum,
            ItemStack output1, ItemStack output2, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createParticleAccelerator(input1, input2, momentum, output1,
                output2, sourceOrder);
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1,
            ItemStack output2) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerParticleAccelerator(sink, id, input1, input2,
                momentum, output1, output2);
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1,
            ItemStack output2, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerParticleAccelerator(sink, id, input1, input2,
                momentum, output1, output2, sourceOrder);
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1, ItemStack output2) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerParticleAccelerator(sink, name, input1, input2,
                momentum, output1, output2);
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1, ItemStack output2,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerParticleAccelerator(sink, name, input1, input2,
                momentum, output1, output2, sourceOrder);
    }

    public static JsonObject createOutgasser(HbmIngredient input, ItemStack solidOutput,
            HbmFluidStack fluidOutput, boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createOutgasser(input, solidOutput, fluidOutput, fusionOnly);
    }

    public static JsonObject createOutgasser(HbmIngredient input, ItemStack solidOutput,
            HbmFluidStack fluidOutput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createOutgasser(input, solidOutput, fluidOutput);
    }

    public static JsonObject createOutgasser(ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createOutgasser(input, solidOutput, fluidOutput, fusionOnly);
    }

    public static JsonObject createOutgasser(TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createOutgasser(input, solidOutput, fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, id, input, solidOutput,
                fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, id, input, solidOutput,
                fluidOutput);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, name, input, solidOutput,
                fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, name, input, solidOutput,
                fluidOutput);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, id, input, solidOutput,
                fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, id, input, solidOutput,
                fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, name, input, solidOutput,
                fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, name, input, solidOutput,
                fluidOutput, fusionOnly);
    }

    public static JsonObject createAnvilConstruction(List<HbmIngredient> inputs, List<HbmItemOutput> outputs,
            int tierLower, int tierUpper, AnvilConstructionRecipe.OverlayType overlay) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createAnvilConstruction(inputs, outputs, tierLower, tierUpper,
                overlay);
    }

    public static JsonObject createAnvilConstruction(HbmIngredient[] inputs, HbmItemOutput[] outputs,
            int tierLower, int tierUpper, AnvilConstructionRecipe.OverlayType overlay) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createAnvilConstruction(inputs, outputs, tierLower, tierUpper,
                overlay);
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int tierLower,
            int tierUpper, AnvilConstructionRecipe.OverlayType overlay) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerAnvilConstruction(sink, id, inputs, outputs, tierLower,
                tierUpper, overlay);
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient[] inputs, HbmItemOutput[] outputs, int tierLower, int tierUpper,
            AnvilConstructionRecipe.OverlayType overlay) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerAnvilConstruction(sink, id, inputs, outputs, tierLower,
                tierUpper, overlay);
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int tierLower, int tierUpper,
            AnvilConstructionRecipe.OverlayType overlay) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerAnvilConstruction(sink, name, inputs, outputs,
                tierLower, tierUpper, overlay);
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputs, HbmItemOutput[] outputs, int tierLower, int tierUpper,
            AnvilConstructionRecipe.OverlayType overlay) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerAnvilConstruction(sink, name, inputs, outputs,
                tierLower, tierUpper, overlay);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPedestal(inputItems, output);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output, int condition) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPedestal(inputItems, output, condition);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output,
            PedestalRecipe.ExtraCondition extra) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPedestal(inputItems, output, extra);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output,
            PedestalRecipe.ExtraCondition extra, int set, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPedestal(inputItems, output, extra, set, sourceOrder);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, HbmItemOutput output,
            PedestalRecipe.ExtraCondition extra, int set, int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPedestal(inputItems, output, extra, set, sourceOrder);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputItems, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPedestal(sink, id, inputItems, output);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputItems, ItemStack output, int condition) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPedestal(sink, id, inputItems, output, condition);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputItems, ItemStack output, PedestalRecipe.ExtraCondition extra, int set,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPedestal(sink, id, inputItems, output, extra, set,
                sourceOrder);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputItems, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPedestal(sink, name, inputItems, output);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputItems, ItemStack output, int condition) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPedestal(sink, name, inputItems, output, condition);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputItems, ItemStack output, PedestalRecipe.ExtraCondition extra, int set,
            int sourceOrder) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPedestal(sink, name, inputItems, output, extra, set,
                sourceOrder);
    }

    public static void registerPress(ItemPressStamp.StampType stamp, AStack input, ItemStack output) {
        enqueueLegacyDirect("press", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp,
                        modernIngredient(input).ingredient(), output, sourceOrder));
    }

    public static void registerBlastFurnace(Object[] inputs, ItemStack output) {
        if (inputs == null || inputs.length != 2) {
            return;
        }
        enqueueLegacyDirect("difurnace", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerDiFurnace(sink, id,
                        new HbmIngredient[] {
                                modernIngredient(inputs[0]),
                                modernIngredient(inputs[1])
                        }, output));
    }

    public static void registerShredder(AStack input, ItemStack output) {
        enqueueLegacyDirect("shredder", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, id,
                        ItemProcessingRecipe.Machine.SHREDDER, modernIngredient(input),
                        modernItemOutputs(new ItemStack[] { output }), null, 0, 0.0F, sourceOrder));
    }

    public static void registerSoldering(ItemStack output, int time, long power, FluidStack fluid, AStack[] toppings,
            AStack[] pcb, AStack[] solder) {
        enqueueLegacyDirect("soldering_station", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerSoldering(sink, id, output, time, power,
                        modernFluid(fluid), modernIngredients(first(toppings, 3)), modernIngredients(first(pcb, 2)),
                        modernIngredients(first(solder, 1)), sourceOrder));
    }

    public static void registerAssembler(String name, boolean named, ItemStack icon, int duration, long power,
            AStack[] inputItems, FluidStack inputFluids, IOutput[] outputItems, FluidStack outputFluids) {
        enqueueLegacyDirect("assembly_machine", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerGeneric(sink, id,
                        GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, name, named, icon, duration, power,
                        modernIngredients(inputItems), new HbmFluidStack[] { modernFluid(inputFluids) },
                        modernOutputs(outputItems), new HbmFluidStack[] { modernFluid(outputFluids) }, sourceOrder));
    }

    public static void registerChemicalPlant(String name, boolean named, ItemStack icon, int duration, long power,
            AStack[] inputItems, FluidStack[] inputFluids, IOutput[] outputItems, FluidStack[] outputFluids) {
        enqueueLegacyDirect("chemical_plant", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerGeneric(sink, id,
                        GenericMachineRecipe.Machine.CHEMICAL_PLANT, name, named, icon, duration, power,
                        modernIngredients(inputItems), modernFluidStacks(inputFluids), modernOutputs(outputItems),
                        modernFluidStacks(outputFluids), sourceOrder));
    }

    public static void registerCombination(AStack input, ItemStack output, FluidStack fluid) {
        if ((output == null || output.isEmpty()) && fluid == null) {
            return;
        }
        enqueueLegacyDirect("combination_oven", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCombination(sink, id, modernIngredient(input),
                        output, modernFluid(fluid)));
    }

    public static void registerCrucible(int index, String name, int frequency, ItemStack icon, MaterialStack[] input,
            MaterialStack[] output) {
        enqueueLegacyDirect("crucible", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCrucible(sink, id, name, name, icon, frequency,
                        input, output, sourceOrder));
    }

    public static void registerCentrifuge(AStack input, ItemStack[] outputs) {
        enqueueLegacyDirect("centrifuge", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, id,
                        ItemProcessingRecipe.Machine.CENTRIFUGE, modernIngredient(input),
                        modernItemOutputs(first(outputs, 4)), null, 0, 0.0F, sourceOrder));
    }

    public static void registerCrystallizer(AStack input, ItemStack output, int time, float productivity,
            FluidStack fluid) {
        enqueueLegacyDirect("crystallizer", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, id, modernIngredient(input),
                        output, time, productivity, modernFluid(fluid), sourceOrder));
    }

    public static void registerFraction(FluidType input, FluidStack[] output) {
        if (output == null || output.length != 2) {
            return;
        }
        enqueueLegacyDirect("fraction_tower", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerFraction(sink, id, input,
                        modernFluidStacks(output)));
    }

    public static void registerCracking(FluidType input, FluidStack[] output) {
        if (output == null || output.length != 2) {
            return;
        }
        enqueueLegacyDirect("catalytic_cracker", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCracking(sink, id, input,
                        modernFluidStacks(output)));
    }

    public static void registerReforming(FluidType input, FluidStack[] output) {
        FluidStack[] safeOutput = first(output, 3);
        if (safeOutput == null || safeOutput.length < 3) {
            return;
        }
        enqueueLegacyDirect("catalytic_reformer", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerReforming(sink, id, input,
                        modernFluidStacks(safeOutput)));
    }

    public static void registerHydrotreating(FluidType input, FluidStack hydrogen, FluidStack[] output) {
        FluidStack[] safeOutput = first(output, 2);
        if (safeOutput == null || safeOutput.length < 2) {
            return;
        }
        enqueueLegacyDirect("hydrotreater", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerHydrotreating(sink, id, input,
                        modernFluid(hydrogen), modernFluidStacks(safeOutput)));
    }

    public static void registerLiquefaction(AStack input, FluidStack output) {
        enqueueLegacyDirect("liquefaction", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, modernIngredient(input).ingredient(),
                        modernFluid(output), sourceOrder));
    }

    public static void registerSolidifying(FluidStack input, ItemStack output) {
        enqueueLegacyDirect("solidifier", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerSolidifying(sink, id, modernFluid(input), output));
    }

    public static void registerCoker(FluidStack input, ItemStack output, FluidStack fluid) {
        enqueueLegacyDirect("coker", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCoker(sink, id, modernFluid(input), output,
                        modernFluid(fluid)));
    }

    public static void registerCokerAuto(FluidType input, FluidType output) {
        enqueueLegacyDirect("coker", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCokerAuto(sink, id, input, output));
    }

    public static void registerPyro(FluidStack inputFluid, AStack inputItem, FluidStack outputFluid,
            ItemStack outputItem, int duration) {
        enqueueLegacyDirect("pyro_oven", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerPyro(sink, id, duration, modernIngredient(inputItem),
                        modernFluid(inputFluid), itemOutputOrNull(outputItem), modernFluid(outputFluid), sourceOrder));
    }

    public static void registerPyroAuto(FluidType input) {
        enqueueLegacyDirect("pyro_oven", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerPyroAuto(sink, id, input, sourceOrder));
    }

    public static void registerBreeder(ComparableStack input, ItemStack output, int flux) {
        enqueueLegacyDirect("breeding_reactor", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerBreeder(sink, id, modernIngredient(input), output,
                        flux, sourceOrder));
    }

    public static void registerCyclotron(ComparableStack box, AStack target, ItemStack output, int antimatter) {
        enqueueLegacyDirect("cyclotron", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCyclotron(sink, id, modernIngredient(box),
                        modernIngredient(target), output, antimatter, sourceOrder));
    }

    public static void registerFuelPool(ComparableStack input, ItemStack output) {
        enqueueLegacyDirect("fuel_pool", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerFuelPool(sink, id, modernIngredient(input),
                        output, sourceOrder));
    }

    @Deprecated(forRemoval = false)
    public static void registerOutgasser(AStack input, ItemStack output, FluidStack fluid) {
        registerOutgasser(input, output, fluid, false);
    }

    public static void registerOutgasser(AStack input, ItemStack output, FluidStack fluid, boolean fusionOnly) {
        enqueueLegacyDirect("outgasser", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerOutgasser(sink, id, modernIngredient(input),
                        output, modernFluid(fluid), fusionOnly, sourceOrder));
    }

    public static void registerCompressor(FluidStack input, FluidStack output, int time) {
        enqueueLegacyDirect("compressor", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerCompressor(sink, id, modernFluid(input),
                        modernFluid(output), time));
    }

    public static void registerElectrolyzerFluid(FluidStack input, FluidStack[] output, ItemStack[] byproduct,
            int time) {
        FluidStack[] safeOutput = first(output, 2);
        if (safeOutput == null || safeOutput.length < 2) {
            return;
        }
        enqueueLegacyDirect("electrolyzer_fluid", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerElectrolyzerFluid(sink, id, modernFluid(input),
                        modernFluid(safeOutput[0]), modernFluid(safeOutput[1]), first(byproduct, 3), time));
    }

    public static void registerElectrolyzerMetal(AStack input, MaterialStack[] output, ItemStack[] byproduct,
            int time) {
        MaterialStack[] safeOutput = first(output, 2);
        enqueueLegacyDirect("electrolyzer_metal", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerElectrolyzerMetal(sink, id, modernIngredient(input),
                        safeOutput[0], safeOutput[1], first(byproduct, 6), time));
    }

    public static void registerArcWelder(ItemStack output, int time, long power, FluidStack fluid, AStack[] inputs) {
        enqueueLegacyDirect("arc_welder", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerGeneric(sink, id,
                        GenericMachineRecipe.Machine.ARC_WELDER, "legacy.direct." + sourceOrder, false, output, time,
                        power, modernIngredients(first(inputs, 3)), new HbmFluidStack[] { modernFluid(fluid) },
                        modernItemOutputs(new ItemStack[] { output }), new HbmFluidStack[0], sourceOrder));
    }

    public static void registerRotaryFurnace(MaterialStack output, int time, int steam, FluidStack fluid,
            AStack[] inputs) {
        enqueueLegacyDirect("rotary_furnace", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerRotaryFurnace(sink, id, output, time, steam,
                        modernFluid(fluid), modernIngredients(first(inputs, 3)), sourceOrder));
    }

    public static void registerExposureChamber(AStack particle, AStack input, ItemStack output) {
        enqueueLegacyDirect("exposure_chamber", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerExposureChamber(sink, id,
                        modernIngredient(particle), modernIngredient(input), output, sourceOrder));
    }

    public static void registerFusionReactor(String name, int time, long power, long klystron, long plasma,
            double neutrons, FluidStack[] inputs, ItemStack outputItem, FluidStack outputFluid) {
        enqueueLegacyDirect("fusion_reactor", (sink, id, sourceOrder) -> {
            JsonObject json = com.hbm.ntm.compat.CompatRecipeRegistry.createFusionReactor(id, name, time, power,
                    klystron, plasma, neutrons, modernFluidStacks(first(inputs, 3)), outputItem,
                    modernFluid(outputFluid));
            json.addProperty("source_order", sourceOrder);
            sink.accept(id, json);
        });
    }

    public static void registerParticleAccelerator(AStack[] input, int momentum, ItemStack[] output) {
        AStack[] safeInput = first(input, 2);
        ItemStack[] safeOutput = first(output, 2);
        if (safeInput == null || safeInput.length < 2 || safeOutput == null || safeOutput.length < 1) {
            return;
        }
        enqueueLegacyDirect("particle_accelerator", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerParticleAccelerator(sink, id,
                        modernIngredient(safeInput[0]), modernIngredient(safeInput[1]), momentum, safeOutput[0],
                        safeOutput.length > 1 ? safeOutput[1] : null, sourceOrder));
    }

    public static void registerAmmoPress(ItemStack output, AStack[] input) {
        if (input == null || input.length != 9) {
            return;
        }
        enqueueLegacyDirect("ammo_press", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerAmmoPress(sink, id, modernIngredients(input),
                        output, sourceOrder));
    }

    public static void registerAnvilConstruction(AStack[] input, AnvilOutput[] output, int tier, int overlayIndex) {
        enqueueLegacyDirect("anvil_construction", (sink, id, sourceOrder) -> {
            JsonObject json = com.hbm.ntm.compat.CompatRecipeRegistry.createAnvilConstruction(
                    modernIngredients(input), modernAnvilOutputs(output), tier, -1, legacyAnvilOverlay(overlayIndex));
            json.addProperty("source_order", sourceOrder);
            sink.accept(id, json);
        });
    }

    public static void registerAnvilConstruction(AStack[] input, AnvilOutput[] output, int tierLower, int tierUpper,
            int overlayIndex) {
        enqueueLegacyDirect("anvil_construction", (sink, id, sourceOrder) -> {
            JsonObject json = com.hbm.ntm.compat.CompatRecipeRegistry.createAnvilConstruction(
                    modernIngredients(input), modernAnvilOutputs(output), tierLower, tierUpper,
                    legacyAnvilOverlay(overlayIndex));
            json.addProperty("source_order", sourceOrder);
            sink.accept(id, json);
        });
    }

    public static void registerPedestal(ItemStack output, AStack[] input) {
        registerPedestal(output, input, 0);
    }

    public static void registerPedestal(ItemStack output, AStack[] input, int condition) {
        AStack[] safeInput = first(input, 9);
        if (safeInput == null || safeInput.length < 9) {
            return;
        }
        enqueueLegacyDirect("pedestal", (sink, id, sourceOrder) ->
                com.hbm.ntm.compat.CompatRecipeRegistry.registerPedestal(sink, id, modernIngredients(safeInput),
                        output, legacyPedestalExtra(condition), 0, sourceOrder));
    }

    public static void registerArcFurnace(AStack input, ItemStack output, MaterialStack fluid) {
        if ((output == null || output.isEmpty()) && (fluid == null || fluid.isEmpty())) {
            return;
        }
        enqueueLegacyDirect("arc_furnace", (sink, id, sourceOrder) -> {
            JsonObject json = com.hbm.ntm.compat.CompatRecipeRegistry.createArcFurnace(id,
                    "legacy.direct." + sourceOrder, modernIngredient(input), output, fluid);
            json.addProperty("source_order", sourceOrder);
            sink.accept(id, json);
        });
    }

    public static com.hbm.ntm.compat.CompatRecipeRegistry.Diagnostics diagnostics() {
        return com.hbm.ntm.compat.CompatRecipeRegistry.diagnostics();
    }

    public static List<String> supportedRecipeFacades() {
        return com.hbm.ntm.compat.CompatRecipeRegistry.supportedRecipeFacades();
    }

    public static List<String> supportedLegacyRecipeFacades() {
        return com.hbm.ntm.compat.CompatRecipeRegistry.supportedLegacyRecipeFacades();
    }

    public static List<String> deferredLegacyRecipeFacades() {
        return com.hbm.ntm.compat.CompatRecipeRegistry.deferredLegacyRecipeFacades();
    }

    public static List<com.hbm.ntm.compat.CompatRecipeRegistry.RecipeFacadeStatus> recipeFacadeStatuses() {
        return com.hbm.ntm.compat.CompatRecipeRegistry.recipeFacadeStatuses();
    }

    public static Optional<com.hbm.ntm.compat.CompatRecipeRegistry.RecipeFacadeStatus> recipeFacadeStatus(
            String legacyMethodOrModernFacade) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.recipeFacadeStatus(legacyMethodOrModernFacade);
    }

    public static boolean isRecipeFacadeSupported(String legacyMethodOrModernFacade) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.isRecipeFacadeSupported(legacyMethodOrModernFacade);
    }

    public static com.hbm.ntm.compat.CompatRecipeRegistry.RecipeFacadeCoverage recipeFacadeCoverage() {
        return com.hbm.ntm.compat.CompatRecipeRegistry.recipeFacadeCoverage();
    }

    /** NOP in 1.7.10. */
    @Deprecated(forRemoval = false)
    public static void registerAssembler(ItemStack output, com.hbm.inventory.RecipesCommon.AStack[] input, int time) {
    }

    /** NOP in 1.7.10. */
    @Deprecated(forRemoval = false)
    public static void registerAssembler(ItemStack output, com.hbm.inventory.RecipesCommon.AStack[] input, int time,
            Item... folder) {
    }

    /** NOP in 1.7.10. */
    @Deprecated(forRemoval = false)
    public static void registerChemplant(int id, String name, int duration,
            com.hbm.inventory.RecipesCommon.AStack[] inputItems, com.hbm.inventory.FluidStack[] inputFluids,
            ItemStack[] outputItems, com.hbm.inventory.FluidStack[] outputFluids) {
    }

    private static void enqueueLegacyDirect(String folder, LegacyDirectEmission emission) {
        int sourceOrder = LEGACY_DIRECT_RECIPE_COUNTER.getAndIncrement();
        ResourceLocation id = compatRecipeId(folder, "legacy_direct_" + sourceOrder);
        registerRecipeRegisterListener(sink -> emission.emit(sink, id, sourceOrder));
    }

    private static HbmIngredient modernIngredient(Object input) {
        if (input instanceof HbmIngredient ingredient) {
            return ingredient;
        }
        if (input instanceof AStack stack) {
            return modernIngredient(stack);
        }
        if (input instanceof DictFrame frame) {
            return modernIngredient(frame);
        }
        if (input instanceof ItemStack stack) {
            if (stack.isEmpty()) {
                throw new IllegalArgumentException("Legacy compat recipe input cannot be empty");
            }
            return HbmIngredient.exact(stack);
        }
        if (input instanceof ItemLike item) {
            return HbmIngredient.of(item, 1);
        }
        if (input instanceof String oreName) {
            return HbmIngredient.legacyOre(oreName, 1);
        }
        if (input instanceof Ingredient ingredient) {
            return HbmIngredient.of(ingredient, 1);
        }
        throw new IllegalArgumentException("Unsupported legacy compat recipe input: " + input);
    }

    private static HbmIngredient modernIngredient(AStack input) {
        if (input == null) {
            return null;
        }
        if (input instanceof OreDictStack ore) {
            return HbmIngredient.legacyOre(ore.name, ore.stacksize);
        }
        if (input instanceof NBTStack nbt) {
            return HbmIngredient.partialNbt(nbt.toStack());
        }
        if (input instanceof ComparableStack comparable) {
            if (comparable.meta == HbmIngredient.WILDCARD_META) {
                return HbmIngredient.of(comparable.item, comparable.stacksize);
            }
            return HbmIngredient.exact(comparable.toStack());
        }
        List<ItemStack> stacks = input.extractForNEI();
        if (stacks == null || stacks.isEmpty()) {
            throw new IllegalArgumentException("Legacy compat recipe input has no display stacks");
        }
        return HbmIngredient.of(Ingredient.of(stacks.toArray(ItemStack[]::new)), Math.max(1, input.stacksize));
    }

    private static HbmIngredient modernIngredient(DictFrame frame) {
        JsonArray alternatives = new JsonArray();
        addLegacyOreTag(alternatives, frame.ingot());
        addLegacyOreTag(alternatives, frame.plate());
        addLegacyOreTag(alternatives, frame.gem());
        addLegacyOreTag(alternatives, frame.dust());
        return HbmIngredient.of(Ingredient.fromJson(alternatives), 1);
    }

    private static void addLegacyOreTag(JsonArray alternatives, String legacyOreName) {
        JsonObject tag = new JsonObject();
        tag.addProperty("tag", LegacyOreDictionaryMappings.itemTagId(legacyOreName).toString());
        alternatives.add(tag);
    }

    private static HbmIngredient[] modernIngredients(AStack[] inputs) {
        if (inputs == null) {
            return new HbmIngredient[0];
        }
        HbmIngredient[] modern = new HbmIngredient[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            modern[i] = modernIngredient(inputs[i]);
        }
        return modern;
    }

    private static HbmItemOutput[] modernOutputs(IOutput[] outputs) {
        if (outputs == null) {
            return new HbmItemOutput[0];
        }
        List<HbmItemOutput> modern = new ArrayList<>();
        for (IOutput output : outputs) {
            HbmItemOutput converted = modernOutput(output);
            if (converted != null) {
                modern.add(converted);
            }
        }
        return modern.toArray(HbmItemOutput[]::new);
    }

    private static HbmItemOutput modernOutput(IOutput output) {
        if (output == null) {
            return null;
        }
        if (output instanceof ChanceOutput chance) {
            return chance.stack == null || chance.stack.isEmpty() ? null : HbmItemOutput.chance(chance.stack,
                    chance.chance);
        }
        if (output instanceof ChanceOutputMulti multi) {
            List<HbmItemOutput.Entry> entries = new ArrayList<>();
            for (ChanceOutput chance : multi.pool) {
                if (chance != null && chance.stack != null && !chance.stack.isEmpty()) {
                    entries.add(new HbmItemOutput.Entry(chance.stack, chance.chance, chance.itemWeight));
                }
            }
            return entries.isEmpty() ? null : HbmItemOutput.oneOf(entries);
        }
        ItemStack single = output.getSingle();
        if (single != null && !single.isEmpty()) {
            return HbmItemOutput.of(single);
        }
        ItemStack[] possibilities = output.getAllPossibilities();
        if (possibilities == null || possibilities.length == 0) {
            return null;
        }
        List<HbmItemOutput.Entry> entries = new ArrayList<>();
        for (ItemStack stack : possibilities) {
            if (stack != null && !stack.isEmpty()) {
                entries.add(new HbmItemOutput.Entry(stack, 1.0F, 1));
            }
        }
        return entries.isEmpty() ? null : HbmItemOutput.oneOf(entries);
    }

    private static HbmItemOutput[] modernItemOutputs(ItemStack[] outputs) {
        if (outputs == null) {
            return new HbmItemOutput[0];
        }
        List<HbmItemOutput> modern = new ArrayList<>();
        for (ItemStack output : outputs) {
            if (output != null && !output.isEmpty()) {
                modern.add(HbmItemOutput.of(output));
            }
        }
        return modern.toArray(HbmItemOutput[]::new);
    }

    private static HbmItemOutput[] modernAnvilOutputs(AnvilOutput[] outputs) {
        if (outputs == null) {
            return new HbmItemOutput[0];
        }
        List<HbmItemOutput> modern = new ArrayList<>();
        for (AnvilOutput output : outputs) {
            if (output != null && output.stack != null && !output.stack.isEmpty()) {
                modern.add(HbmItemOutput.chance(output.stack, output.chance));
            }
        }
        return modern.toArray(HbmItemOutput[]::new);
    }

    private static HbmItemOutput itemOutputOrNull(ItemStack output) {
        return output == null || output.isEmpty() ? null : HbmItemOutput.of(output);
    }

    private static AnvilConstructionRecipe.OverlayType legacyAnvilOverlay(int overlayIndex) {
        com.hbm.inventory.recipes.anvil.AnvilRecipes.OverlayType legacy =
                EnumUtil.grabEnumSafely(com.hbm.inventory.recipes.anvil.AnvilRecipes.OverlayType.class,
                        overlayIndex);
        return AnvilConstructionRecipe.OverlayType.valueOf(legacy.name());
    }

    private static PedestalRecipe.ExtraCondition legacyPedestalExtra(int condition) {
        PedestalRecipe.ExtraCondition[] values = PedestalRecipe.ExtraCondition.values();
        return values[Math.floorMod(condition, values.length)];
    }

    private static HbmFluidStack modernFluid(FluidStack stack) {
        return stack == null ? null : stack.toModern();
    }

    public static ResourceLocation compatRecipeId(String recipeFolder, String name) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.compatRecipeId(recipeFolder, name);
    }

    private static HbmFluidStack[] modernFluidStacks(com.hbm.inventory.FluidStack[] stacks) {
        if (stacks == null) {
            return null;
        }
        HbmFluidStack[] modern = new HbmFluidStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            modern[i] = stacks[i] == null ? null : stacks[i].toModern();
        }
        return modern;
    }

    private static <T> T[] first(T[] array, int amount) {
        if (array == null || array.length <= amount) {
            return array;
        }
        return Arrays.copyOf(array, amount);
    }

    @FunctionalInterface
    private interface LegacyDirectEmission {
        void emit(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id, int sourceOrder);
    }

    @FunctionalInterface
    public interface RecipeRegisterListener extends com.hbm.ntm.compat.CompatRecipeRegistry.RecipeRegisterListener {
    }

    @FunctionalInterface
    public interface RecipeSink extends com.hbm.ntm.compat.CompatRecipeRegistry.RecipeSink {
    }

    private CompatRecipeRegistry() {
    }
}
