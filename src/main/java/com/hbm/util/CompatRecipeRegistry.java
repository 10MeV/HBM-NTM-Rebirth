package com.hbm.util;

import com.google.gson.JsonObject;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import java.util.List;
import java.util.Optional;
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

    public static JsonObject createPress(ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPress(stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, id, stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPress(sink, name, stamp, input, output);
    }

    public static JsonObject createLiquefaction(Ingredient input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output);
    }

    public static JsonObject createLiquefaction(ItemLike input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output);
    }

    public static JsonObject createLiquefaction(TagKey<Item> input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createLiquefaction(input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            Ingredient input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, id, input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            Ingredient input, HbmFluidStack output) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerLiquefaction(sink, name, input, output);
    }

    public static JsonObject createPyro(int duration, HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createPyro(duration, inputItem, inputFluid, outputItem,
                outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem,
            HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyro(sink, id, duration, inputItem, inputFluid,
                outputItem, outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerPyro(sink, name, duration, inputItem, inputFluid,
                outputItem, outputFluid);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createItemProcessing(machine, input, outputs, fluidInput,
                duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, id, machine, input, outputs,
                fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, List<HbmItemOutput> outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerItemProcessing(sink, name, machine, input, outputs,
                fluidInput, duration, productivity);
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

    public static JsonObject createCrystallizer(HbmIngredient input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput);
    }

    public static JsonObject createCrystallizer(ItemLike input, ItemStack output, int duration, float productivity,
            HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.createCrystallizer(input, output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, id, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, id, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.registerCrystallizer(sink, name, input, output, duration,
                productivity, fluidInput);
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

    public static ResourceLocation compatRecipeId(String recipeFolder, String name) {
        return com.hbm.ntm.compat.CompatRecipeRegistry.compatRecipeId(recipeFolder, name);
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
