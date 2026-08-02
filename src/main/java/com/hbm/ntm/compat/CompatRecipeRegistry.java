package com.hbm.ntm.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidCompressorRecipes;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.AnvilSmithingRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.OilProcessingRecipe;
import com.hbm.ntm.recipe.PedestalRecipe;
import com.hbm.ntm.registry.ModItems;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Modernized compat recipe facade. It builds datapack-compatible recipe JSON
 * and records external listeners; it does not mutate the live RecipeManager.
 */
public final class CompatRecipeRegistry {
    private static final List<com.hbm.ntm.api.recipe.RecipeRegisterListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile int lastInvokedListeners;
    private static volatile int lastFailedListeners;
    private static volatile int lastEmittedRecipes;

    public static void registerRecipeRegisterListener(com.hbm.ntm.api.recipe.RecipeRegisterListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static boolean unregisterRecipeRegisterListener(com.hbm.ntm.api.recipe.RecipeRegisterListener listener) {
        return listener != null && LISTENERS.remove(listener);
    }

    public static void emitRecipeRegisterListeners(com.hbm.ntm.api.recipe.RecipeSink sink) {
        lastInvokedListeners = 0;
        lastFailedListeners = 0;
        lastEmittedRecipes = 0;
        com.hbm.ntm.api.recipe.RecipeSink countingSink = (id, recipe) -> {
            sink.accept(id, recipe);
            lastEmittedRecipes++;
        };
        for (com.hbm.ntm.api.recipe.RecipeRegisterListener listener : LISTENERS) {
            try {
                listener.registerRecipes(countingSink);
                lastInvokedListeners++;
            } catch (RuntimeException exception) {
                lastFailedListeners++;
                HbmNtm.LOGGER.warn("HBM compat recipe listener failed.", exception);
            }
        }
    }

    public static JsonObject createAssembler(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack inputFluid,
            HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return createGeneric(GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, id, name, named, icon, duration, power,
                list(inputItems), singleFluid(inputFluid), list(outputItems), singleFluid(outputFluid));
    }

    public static ResourceLocation registerAssembler(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack inputFluid, HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return emit(sink, id, createAssembler(id, name, named, icon, duration, power, inputItems, inputFluid,
                outputItems, outputFluid));
    }

    public static ResourceLocation registerAssembler(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack inputFluid, HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        ResourceLocation id = compatRecipeId("assembly_machine", name);
        return registerAssembler(sink, id, name, named, icon, duration, power, inputItems, inputFluid, outputItems,
                outputFluid);
    }

    public static JsonObject createChemicalPlant(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack[] inputFluids,
            HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return createGeneric(GenericMachineRecipe.Machine.CHEMICAL_PLANT, id, name, named, icon, duration, power,
                list(inputItems), fluidList(inputFluids), list(outputItems), fluidList(outputFluids));
    }

    public static ResourceLocation registerChemicalPlant(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return emit(sink, id, createChemicalPlant(id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids));
    }

    public static ResourceLocation registerChemicalPlant(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        ResourceLocation id = compatRecipeId("chemical_plant", name);
        return registerChemicalPlant(sink, id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids);
    }

    public static JsonObject createPurex(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack[] inputFluids,
            HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return createGeneric(GenericMachineRecipe.Machine.PUREX, id, name, named, icon, duration, power,
                list(inputItems), fluidList(inputFluids), list(outputItems), fluidList(outputFluids));
    }

    public static ResourceLocation registerPurex(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return emit(sink, id, createPurex(id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids));
    }

    public static ResourceLocation registerPurex(com.hbm.ntm.api.recipe.RecipeSink sink, String name, boolean named,
            ItemStack icon, int duration, long power, HbmIngredient[] inputItems, HbmFluidStack[] inputFluids,
            HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        ResourceLocation id = compatRecipeId("purex", name);
        return registerPurex(sink, id, name, named, icon, duration, power, inputItems, inputFluids, outputItems,
                outputFluids);
    }

    public static JsonObject createPrecass(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack inputFluid,
            HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return createGeneric(GenericMachineRecipe.Machine.PRECASS, id, name, named, icon, duration, power,
                list(inputItems), singleFluid(inputFluid), list(outputItems), singleFluid(outputFluid));
    }

    public static ResourceLocation registerPrecass(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack inputFluid, HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return emit(sink, id, createPrecass(id, name, named, icon, duration, power, inputItems, inputFluid,
                outputItems, outputFluid));
    }

    public static ResourceLocation registerPrecass(com.hbm.ntm.api.recipe.RecipeSink sink, String name, boolean named,
            ItemStack icon, int duration, long power, HbmIngredient[] inputItems, HbmFluidStack inputFluid,
            HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        ResourceLocation id = compatRecipeId("precass", name);
        return registerPrecass(sink, id, name, named, icon, duration, power, inputItems, inputFluid, outputItems,
                outputFluid);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return createPress(stamp, HbmIngredient.of(input, 1), output, Integer.MAX_VALUE);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, Ingredient input, ItemStack output,
            int sourceOrder) {
        return createPress(stamp, HbmIngredient.of(input, 1), output, sourceOrder);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack output) {
        return createPress(stamp, input, output, Integer.MAX_VALUE);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack output,
            int sourceOrder) {
        JsonObject json = new JsonObject();
        Objects.requireNonNull(stamp, "stamp");
        Objects.requireNonNull(input, "input");
        requireItemOutput(output, "press output");
        json.addProperty("type", ModRecipes.PRESS.serializer().getId().toString());
        json.add("input", input.toJson());
        json.addProperty("stamp", stamp.getSerializedName());
        json.add("result", HbmItemOutput.of(output).toJson());
        if (sourceOrder != Integer.MAX_VALUE) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return emit(sink, id, createPress(stamp, input, output));
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output, int sourceOrder) {
        return emit(sink, id, createPress(stamp, input, output, sourceOrder));
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack output) {
        return emit(sink, id, createPress(stamp, input, output));
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack output, int sourceOrder) {
        return emit(sink, id, createPress(stamp, input, output, sourceOrder));
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output, int sourceOrder) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack output) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, HbmIngredient input, ItemStack output, int sourceOrder) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output, sourceOrder);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return createPress(stamp, Ingredient.of(input), output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, ItemLike input, ItemStack output,
            int sourceOrder) {
        return createPress(stamp, Ingredient.of(input), output, sourceOrder);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return createPress(stamp, Ingredient.of(input), output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output,
            int sourceOrder) {
        return createPress(stamp, Ingredient.of(input), output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return registerPress(sink, id, stamp, Ingredient.of(input), output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output, int sourceOrder) {
        return registerPress(sink, id, stamp, Ingredient.of(input), output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return registerPress(sink, id, stamp, Ingredient.of(input), output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output, int sourceOrder) {
        return registerPress(sink, id, stamp, Ingredient.of(input), output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output, int sourceOrder) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output, sourceOrder);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output, int sourceOrder) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output, sourceOrder);
    }

    public static JsonObject createLiquefaction(Ingredient input, HbmFluidStack output) {
        return createLiquefaction(input, output, Integer.MAX_VALUE);
    }

    public static JsonObject createLiquefaction(Ingredient input, HbmFluidStack output, int sourceOrder) {
        JsonObject json = new JsonObject();
        Objects.requireNonNull(input, "input");
        requireFluidOutput(output, "liquefaction output");
        json.addProperty("type", ModRecipes.LIQUEFACTION.serializer().getId().toString());
        json.add("ingredient", input.toJson());
        json.add("output", fluid(output));
        if (sourceOrder != Integer.MAX_VALUE) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            Ingredient input, HbmFluidStack output) {
        return registerLiquefaction(sink, id, input, output, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            Ingredient input, HbmFluidStack output, int sourceOrder) {
        return emit(sink, id, createLiquefaction(input, output, sourceOrder));
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            Ingredient input, HbmFluidStack output) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            Ingredient input, HbmFluidStack output, int sourceOrder) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output, sourceOrder);
    }

    public static JsonObject createLiquefaction(ItemLike input, HbmFluidStack output) {
        return createLiquefaction(Ingredient.of(input), output);
    }

    public static JsonObject createLiquefaction(ItemLike input, HbmFluidStack output, int sourceOrder) {
        return createLiquefaction(Ingredient.of(input), output, sourceOrder);
    }

    public static JsonObject createLiquefaction(TagKey<Item> input, HbmFluidStack output) {
        return createLiquefaction(Ingredient.of(input), output);
    }

    public static JsonObject createLiquefaction(TagKey<Item> input, HbmFluidStack output, int sourceOrder) {
        return createLiquefaction(Ingredient.of(input), output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmFluidStack output) {
        return registerLiquefaction(sink, id, Ingredient.of(input), output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmFluidStack output, int sourceOrder) {
        return registerLiquefaction(sink, id, Ingredient.of(input), output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmFluidStack output) {
        return registerLiquefaction(sink, id, Ingredient.of(input), output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmFluidStack output, int sourceOrder) {
        return registerLiquefaction(sink, id, Ingredient.of(input), output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, HbmFluidStack output) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, HbmFluidStack output, int sourceOrder) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output, sourceOrder);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, HbmFluidStack output) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, HbmFluidStack output, int sourceOrder) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output, sourceOrder);
    }

    public static JsonObject createPyro(int duration, HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return createPyro(duration, inputItem, inputFluid, outputItem, outputFluid, -1);
    }

    public static JsonObject createPyro(int duration, HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid, int sourceOrder) {
        requirePyroEndpoints(inputItem, inputFluid, outputItem, outputFluid);
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.PYRO_OVEN.serializer().getId().toString());
        json.addProperty("duration", Math.max(1, duration));
        if (inputItem != null) {
            json.add("input_item", inputItem.toJson());
        }
        if (inputFluid != null && !inputFluid.isEmpty()) {
            json.add("input_fluid", fluid(inputFluid));
        }
        if (outputItem != null) {
            json.add("output_item", outputItem.toJson());
        }
        if (outputFluid != null && !outputFluid.isEmpty()) {
            json.add("output_fluid", fluid(outputFluid));
        }
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem,
            HbmFluidStack outputFluid) {
        return registerPyro(sink, id, duration, inputItem, inputFluid, outputItem, outputFluid, -1);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem,
            HbmFluidStack outputFluid, int sourceOrder) {
        return emit(sink, id, createPyro(duration, inputItem, inputFluid, outputItem, outputFluid, sourceOrder));
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid, -1);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem, HbmFluidStack outputFluid,
            int sourceOrder) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid, sourceOrder);
    }

    public static JsonObject createPyro(int duration, ItemLike inputItem, HbmFluidStack inputFluid,
            ItemStack outputItem, HbmFluidStack outputFluid) {
        return createPyro(duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1), inputFluid,
                itemOutputOrNull(outputItem), outputFluid);
    }

    public static JsonObject createPyro(int duration, ItemLike inputItem, HbmFluidStack inputFluid,
            ItemStack outputItem, HbmFluidStack outputFluid, int sourceOrder) {
        return createPyro(duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1), inputFluid,
                itemOutputOrNull(outputItem), outputFluid, sourceOrder);
    }

    public static JsonObject createPyro(int duration, TagKey<Item> inputItem, HbmFluidStack inputFluid,
            ItemStack outputItem, HbmFluidStack outputFluid) {
        return createPyro(duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1), inputFluid,
                itemOutputOrNull(outputItem), outputFluid);
    }

    public static JsonObject createPyro(int duration, TagKey<Item> inputItem, HbmFluidStack inputFluid,
            ItemStack outputItem, HbmFluidStack outputFluid, int sourceOrder) {
        return createPyro(duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1), inputFluid,
                itemOutputOrNull(outputItem), outputFluid, sourceOrder);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, ItemLike inputItem, HbmFluidStack inputFluid, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return registerPyro(sink, id, duration, inputItem, inputFluid, outputItem, outputFluid, -1);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, ItemLike inputItem, HbmFluidStack inputFluid, ItemStack outputItem,
            HbmFluidStack outputFluid, int sourceOrder) {
        return registerPyro(sink, id, duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1),
                inputFluid, itemOutputOrNull(outputItem), outputFluid, sourceOrder);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, TagKey<Item> inputItem, HbmFluidStack inputFluid, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return registerPyro(sink, id, duration, inputItem, inputFluid, outputItem, outputFluid, -1);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, TagKey<Item> inputItem, HbmFluidStack inputFluid, ItemStack outputItem,
            HbmFluidStack outputFluid, int sourceOrder) {
        return registerPyro(sink, id, duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1),
                inputFluid, itemOutputOrNull(outputItem), outputFluid, sourceOrder);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            ItemLike inputItem, HbmFluidStack inputFluid, ItemStack outputItem, HbmFluidStack outputFluid) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid, -1);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            ItemLike inputItem, HbmFluidStack inputFluid, ItemStack outputItem, HbmFluidStack outputFluid,
            int sourceOrder) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid, sourceOrder);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            TagKey<Item> inputItem, HbmFluidStack inputFluid, ItemStack outputItem, HbmFluidStack outputFluid) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid, -1);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            TagKey<Item> inputItem, HbmFluidStack inputFluid, ItemStack outputItem, HbmFluidStack outputFluid,
            int sourceOrder) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid, sourceOrder);
    }

    public static JsonObject createPyroAuto(FluidType input) {
        return createPyroAuto(input, Integer.MAX_VALUE);
    }

    public static JsonObject createPyroAuto(FluidType input, int sourceOrder) {
        int amount = pyroAutoAmount(input, 1_440_000L);
        if (amount <= 0) {
            throw new IllegalArgumentException("HBM compat pyro auto input has no positive flammable heat energy");
        }
        return createPyro(60, (HbmIngredient) null, new HbmFluidStack(input, amount),
                HbmItemOutput.of(new ItemStack(ModItems.legacyItem("solid_fuel").get())),
                null, sourceOrder == Integer.MAX_VALUE ? -1 : sourceOrder);
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input) {
        return registerPyroAuto(sink, id, input, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, int sourceOrder) {
        return emit(sink, id, createPyroAuto(input, sourceOrder));
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, FluidType input) {
        return registerPyroAuto(sink, input, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerPyroAuto(com.hbm.ntm.api.recipe.RecipeSink sink, FluidType input,
            int sourceOrder) {
        Objects.requireNonNull(input, "input");
        return registerPyroAuto(sink, compatRecipeId("pyro_oven", "solid_fuel_from_" + input.toPath()), input,
                sourceOrder);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return createItemProcessing(machine, input, outputs, fluidInput, duration, productivity, Integer.MAX_VALUE);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity,
            int sourceOrder) {
        Objects.requireNonNull(machine, "machine");
        Objects.requireNonNull(input, "input");
        List<HbmItemOutput> safeOutputs = nonNullList(outputs);
        requireProcessingOutputs(machine, safeOutputs);
        JsonObject json = new JsonObject();
        json.addProperty("type", itemProcessingSerializerId(machine).toString());
        json.add("input", input.toJson());
        JsonArray outputArray = new JsonArray();
        safeOutputs.forEach(output -> outputArray.add(output.toJson()));
        json.add("outputs", outputArray);
        if (fluidInput != null && !fluidInput.isEmpty()) {
            json.add("fluid", fluid(fluidInput));
        }
        if (duration > 0) {
            json.addProperty("duration", duration);
        }
        if (productivity > 0.0F) {
            json.addProperty("productivity", productivity);
        }
        if (sourceOrder != Integer.MAX_VALUE) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return createItemProcessing(machine, input, outputList(outputs), fluidInput, duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return createItemProcessing(machine, input, outputList(outputs), fluidInput, duration, productivity,
                sourceOrder);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return createItemProcessing(machine, input, itemOutputList(outputs), fluidInput, duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return createItemProcessing(machine, input, itemOutputList(outputs), fluidInput, duration, productivity,
                sourceOrder);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, id, machine, input, outputs, fluidInput, duration, productivity,
                Integer.MAX_VALUE);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return emit(sink, id, createItemProcessing(machine, input, outputs, fluidInput, duration, productivity,
                sourceOrder));
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, id, machine, input, outputList(outputs), fluidInput, duration,
                productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return registerItemProcessing(sink, id, machine, input, outputList(outputs), fluidInput, duration,
                productivity, sourceOrder);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, id, machine, input, itemOutputList(outputs), fluidInput, duration,
                productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return registerItemProcessing(sink, id, machine, input, itemOutputList(outputs), fluidInput, duration,
                productivity, sourceOrder);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, List<HbmItemOutput> outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, List<HbmItemOutput> outputs,
            HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity, sourceOrder);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, HbmItemOutput[] outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, HbmItemOutput[] outputs,
            HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity, sourceOrder);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, ItemStack[] outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, ItemStack[] outputs,
            HbmFluidStack fluidInput, int duration, float productivity, int sourceOrder) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity, sourceOrder);
    }

    public static JsonObject createShredder(HbmIngredient input, ItemStack output) {
        return createItemProcessing(ItemProcessingRecipe.Machine.SHREDDER, input,
                itemOutputList(output), null, 0, 0.0F);
    }

    public static JsonObject createShredder(HbmIngredient input, HbmItemOutput output) {
        return createItemProcessing(ItemProcessingRecipe.Machine.SHREDDER, input,
                outputList(output), null, 0, 0.0F);
    }

    public static JsonObject createShredder(ItemLike input, ItemStack output) {
        return createShredder(HbmIngredient.of(input, 1), output);
    }

    public static JsonObject createShredder(ItemLike input, HbmItemOutput output) {
        return createShredder(HbmIngredient.of(input, 1), output);
    }

    public static JsonObject createShredder(TagKey<Item> input, ItemStack output) {
        return createShredder(HbmIngredient.of(input, 1), output);
    }

    public static JsonObject createShredder(TagKey<Item> input, HbmItemOutput output) {
        return createShredder(HbmIngredient.of(input, 1), output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.SHREDDER, input,
                itemOutputList(output), null, 0, 0.0F);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.SHREDDER, input,
                outputList(output), null, 0, 0.0F);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output) {
        return registerShredder(sink, compatRecipeId("shredder", name), input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output) {
        return registerShredder(sink, compatRecipeId("shredder", name), input, output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output) {
        return registerShredder(sink, id, HbmIngredient.of(input, 1), output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmItemOutput output) {
        return registerShredder(sink, id, HbmIngredient.of(input, 1), output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack output) {
        return registerShredder(sink, id, HbmIngredient.of(input, 1), output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmItemOutput output) {
        return registerShredder(sink, id, HbmIngredient.of(input, 1), output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output) {
        return registerShredder(sink, name, HbmIngredient.of(input, 1), output);
    }

    public static ResourceLocation registerShredder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output) {
        return registerShredder(sink, name, HbmIngredient.of(input, 1), output);
    }

    public static JsonObject createCentrifuge(HbmIngredient input, ItemStack[] outputs) {
        return createItemProcessing(ItemProcessingRecipe.Machine.CENTRIFUGE, input,
                itemOutputList(outputs), null, 0, 0.0F);
    }

    public static JsonObject createCentrifuge(HbmIngredient input, HbmItemOutput[] outputs) {
        return createItemProcessing(ItemProcessingRecipe.Machine.CENTRIFUGE, input,
                outputList(outputs), null, 0, 0.0F);
    }

    public static JsonObject createCentrifuge(ItemLike input, ItemStack[] outputs) {
        return createCentrifuge(HbmIngredient.of(input, 1), outputs);
    }

    public static JsonObject createCentrifuge(ItemLike input, HbmItemOutput[] outputs) {
        return createCentrifuge(HbmIngredient.of(input, 1), outputs);
    }

    public static JsonObject createCentrifuge(TagKey<Item> input, ItemStack[] outputs) {
        return createCentrifuge(HbmIngredient.of(input, 1), outputs);
    }

    public static JsonObject createCentrifuge(TagKey<Item> input, HbmItemOutput[] outputs) {
        return createCentrifuge(HbmIngredient.of(input, 1), outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack[] outputs) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.CENTRIFUGE, input,
                itemOutputList(outputs), null, 0, 0.0F);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput[] outputs) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.CENTRIFUGE, input,
                outputList(outputs), null, 0, 0.0F);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack[] outputs) {
        return registerCentrifuge(sink, compatRecipeId("centrifuge", name), input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput[] outputs) {
        return registerCentrifuge(sink, compatRecipeId("centrifuge", name), input, outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack[] outputs) {
        return registerCentrifuge(sink, id, HbmIngredient.of(input, 1), outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmItemOutput[] outputs) {
        return registerCentrifuge(sink, id, HbmIngredient.of(input, 1), outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack[] outputs) {
        return registerCentrifuge(sink, id, HbmIngredient.of(input, 1), outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmItemOutput[] outputs) {
        return registerCentrifuge(sink, id, HbmIngredient.of(input, 1), outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack[] outputs) {
        return registerCentrifuge(sink, name, HbmIngredient.of(input, 1), outputs);
    }

    public static ResourceLocation registerCentrifuge(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack[] outputs) {
        return registerCentrifuge(sink, name, HbmIngredient.of(input, 1), outputs);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(input, output, duration, productivity, fluidInput, Integer.MAX_VALUE);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return createItemProcessing(ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                itemOutputList(output), fluidInput, duration, productivity, sourceOrder);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(input, output, duration, productivity, fluidInput, Integer.MAX_VALUE);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return createItemProcessing(ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                outputList(output), fluidInput, duration, productivity, sourceOrder);
    }

    public static JsonObject createCrystallizer(ItemLike input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static JsonObject createCrystallizer(ItemLike input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput,
                sourceOrder);
    }

    public static JsonObject createCrystallizer(ItemLike input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static JsonObject createCrystallizer(ItemLike input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput,
                sourceOrder);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput,
                sourceOrder);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput, int sourceOrder) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput,
                sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, input, output, duration, productivity, fluidInput, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                itemOutputList(output), fluidInput, duration, productivity, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, input, output, duration, productivity, fluidInput, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                outputList(output), fluidInput, duration, productivity, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, compatRecipeId("crystallizer", name), input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, compatRecipeId("crystallizer", name), input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, compatRecipeId("crystallizer", name), input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, compatRecipeId("crystallizer", name), input, output, duration,
                productivity, fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, name, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, name, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, name, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput,
            int sourceOrder) {
        return registerCrystallizer(sink, name, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput, sourceOrder);
    }

    public static JsonObject createBlastFurnace(List<HbmIngredient> inputs, List<HbmItemOutput> outputs,
            int duration) {
        List<HbmIngredient> safeInputs = nonNullList(inputs);
        List<HbmItemOutput> safeOutputs = outputList(outputs);
        requireBlastFurnaceShape(safeInputs, safeOutputs);
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.BLAST_FURNACE.serializer().getId().toString());
        json.add("inputs", itemInputs(safeInputs));
        json.add("outputs", itemOutputs(safeOutputs));
        if (duration > 0) {
            json.addProperty("duration", duration);
        }
        return json;
    }

    public static JsonObject createBlastFurnace(HbmIngredient[] inputs, HbmItemOutput[] outputs, int duration) {
        return createBlastFurnace(list(inputs), outputList(outputs), duration);
    }

    public static JsonObject createBlastFurnace(HbmIngredient[] inputs, ItemStack output, int duration) {
        return createBlastFurnace(list(inputs), itemOutputList(output), duration);
    }

    public static JsonObject createBlastFurnace(HbmIngredient firstInput, HbmIngredient secondInput,
            ItemStack output, int duration) {
        return createBlastFurnace(List.of(firstInput, secondInput), itemOutputList(output), duration);
    }

    public static JsonObject createBlastFurnace(HbmIngredient firstInput, HbmIngredient secondInput,
            HbmItemOutput output, int duration) {
        return createBlastFurnace(List.of(firstInput, secondInput), outputList(output), duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int duration) {
        return emit(sink, id, createBlastFurnace(inputs, outputs, duration));
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputs, HbmItemOutput[] outputs, int duration) {
        return registerBlastFurnace(sink, id, list(inputs), outputList(outputs), duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputs, ItemStack output, int duration) {
        return registerBlastFurnace(sink, id, list(inputs), itemOutputList(output), duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient firstInput, HbmIngredient secondInput, ItemStack output, int duration) {
        return registerBlastFurnace(sink, id, List.of(firstInput, secondInput), itemOutputList(output), duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int duration) {
        return registerBlastFurnace(sink, compatRecipeId("blast_furnace", name), inputs, outputs, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputs, HbmItemOutput[] outputs, int duration) {
        return registerBlastFurnace(sink, compatRecipeId("blast_furnace", name), inputs, outputs, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputs, ItemStack output, int duration) {
        return registerBlastFurnace(sink, compatRecipeId("blast_furnace", name), inputs, output, duration);
    }

    public static ResourceLocation registerBlastFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient firstInput, HbmIngredient secondInput, ItemStack output, int duration) {
        return registerBlastFurnace(sink, compatRecipeId("blast_furnace", name), firstInput, secondInput, output,
                duration);
    }

    public static JsonObject createCombination(HbmIngredient input, HbmItemOutput outputItem,
            HbmFluidStack outputFluid) {
        Objects.requireNonNull(input, "input");
        requireCombinationOutput(outputItem, outputFluid);
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.COMBINATION_OVEN.serializer().getId().toString());
        json.add("input", input.toJson());
        if (outputItem != null) {
            json.add("output_item", outputItem.toJson());
        }
        if (outputFluid != null && !outputFluid.isEmpty()) {
            json.add("output_fluid", fluid(outputFluid));
        }
        return json;
    }

    public static JsonObject createCombination(HbmIngredient input, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return createCombination(input, itemOutputOrNull(outputItem), outputFluid);
    }

    public static ResourceLocation registerCombination(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return emit(sink, id, createCombination(input, outputItem, outputFluid));
    }

    public static ResourceLocation registerCombination(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack outputItem, HbmFluidStack outputFluid) {
        return registerCombination(sink, id, input, itemOutputOrNull(outputItem), outputFluid);
    }

    public static ResourceLocation registerCombination(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return registerCombination(sink, compatRecipeId("combination_oven", name), input, outputItem, outputFluid);
    }

    public static ResourceLocation registerCombination(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack outputItem, HbmFluidStack outputFluid) {
        return registerCombination(sink, compatRecipeId("combination_oven", name), input, outputItem, outputFluid);
    }

    public static JsonObject createSoldering(ItemStack output, int duration, long power, HbmFluidStack fluid,
            List<HbmIngredient> toppings, List<HbmIngredient> pcb, List<HbmIngredient> solder, int sourceOrder) {
        requireItemOutput(output, "soldering station output");
        List<HbmIngredient> safeToppings = nonNullList(toppings);
        List<HbmIngredient> safePcb = nonNullList(pcb);
        List<HbmIngredient> safeSolder = nonNullList(solder);
        requireSolderingShape(safeToppings, safePcb, safeSolder);
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.SOLDERING_STATION.serializer().getId().toString());
        json.add("toppings", itemInputs(safeToppings));
        json.add("pcb", itemInputs(safePcb));
        json.add("solder", itemInputs(safeSolder));
        if (fluid != null && !fluid.isEmpty()) {
            json.add("fluid", fluid(fluid));
        }
        json.add("output", HbmItemOutput.of(output).toJson());
        json.addProperty("duration", Math.max(1, duration));
        json.addProperty("consumption", Math.max(1L, power));
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static JsonObject createSoldering(ItemStack output, int duration, long power, HbmFluidStack fluid,
            HbmIngredient[] toppings, HbmIngredient[] pcb, HbmIngredient[] solder, int sourceOrder) {
        return createSoldering(output, duration, power, fluid, list(toppings), list(pcb), list(solder), sourceOrder);
    }

    public static JsonObject createSoldering(ItemStack output, int duration, long power, HbmFluidStack fluid,
            HbmIngredient[] toppings, HbmIngredient[] pcb, HbmIngredient[] solder) {
        return createSoldering(output, duration, power, fluid, toppings, pcb, solder, -1);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack output, int duration, long power, HbmFluidStack fluid, List<HbmIngredient> toppings,
            List<HbmIngredient> pcb, List<HbmIngredient> solder, int sourceOrder) {
        return emit(sink, id, createSoldering(output, duration, power, fluid, toppings, pcb, solder, sourceOrder));
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder, int sourceOrder) {
        return registerSoldering(sink, id, output, duration, power, fluid, list(toppings), list(pcb), list(solder),
                sourceOrder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder) {
        return registerSoldering(sink, id, output, duration, power, fluid, toppings, pcb, solder, -1);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack fluid, List<HbmIngredient> toppings,
            List<HbmIngredient> pcb, List<HbmIngredient> solder, int sourceOrder) {
        return registerSoldering(sink, compatRecipeId("soldering_station", name), output, duration, power, fluid,
                toppings, pcb, solder, sourceOrder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder, int sourceOrder) {
        return registerSoldering(sink, compatRecipeId("soldering_station", name), output, duration, power, fluid,
                toppings, pcb, solder, sourceOrder);
    }

    public static ResourceLocation registerSoldering(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack fluid, HbmIngredient[] toppings,
            HbmIngredient[] pcb, HbmIngredient[] solder) {
        return registerSoldering(sink, name, output, duration, power, fluid, toppings, pcb, solder, -1);
    }

    public static JsonObject createMixer(HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2,
            HbmIngredient solidInput, int duration) {
        return createMixer(output, input1, input2, solidInput, duration, -1);
    }

    public static JsonObject createMixer(HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2,
            HbmIngredient solidInput, int duration, int sourceOrder) {
        requireFluidOutput(output, "mixer output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.MIXER.serializer().getId().toString());
        json.add("output", fluid(output));
        if (input1 != null && !input1.isEmpty()) {
            json.add("input1", fluid(input1));
        }
        if (input2 != null && !input2.isEmpty()) {
            json.add("input2", fluid(input2));
        }
        if (solidInput != null) {
            json.add("solid_input", solidInput.toJson());
        }
        if (duration > 0) {
            json.addProperty("duration", duration);
        }
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration) {
        return registerMixer(sink, id, output, input1, input2, solidInput, duration, -1);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration, int sourceOrder) {
        return emit(sink, id, createMixer(output, input1, input2, solidInput, duration, sourceOrder));
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration) {
        return registerMixer(sink, compatRecipeId("mixer", name), output, input1, input2, solidInput, duration, -1);
    }

    public static ResourceLocation registerMixer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack output, HbmFluidStack input1, HbmFluidStack input2, HbmIngredient solidInput,
            int duration, int sourceOrder) {
        return registerMixer(sink, compatRecipeId("mixer", name), output, input1, input2, solidInput, duration,
                sourceOrder);
    }

    public static JsonObject createCracking(FluidType input, HbmFluidStack[] outputs) {
        return createCracking(new HbmFluidStack(input, 100), outputs);
    }

    public static JsonObject createCracking(HbmFluidStack input, HbmFluidStack[] outputs) {
        HbmFluidStack[] safeOutputs = outputs == null ? new HbmFluidStack[0] : Arrays.copyOf(outputs, outputs.length);
        if (safeOutputs.length != 2) {
            throw new IllegalArgumentException("HBM compat cracking recipe must have exactly two fluid outputs");
        }
        return createCracking(input, safeOutputs[0], safeOutputs[1]);
    }

    public static JsonObject createCracking(HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        requireCrackingShape(input, output1, output2);
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.CATALYTIC_CRACKER.serializer().getId().toString());
        json.add("input", fluid(input));
        json.add("output1", fluid(output1));
        json.add("output2", fluid(output2));
        return json;
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, HbmFluidStack[] outputs) {
        return emit(sink, id, createCracking(input, outputs));
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return emit(sink, id, createCracking(input, outputs));
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        return emit(sink, id, createCracking(input, output1, output2));
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack[] outputs) {
        return registerCracking(sink, compatRecipeId("catalytic_cracker", name), input, outputs);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return registerCracking(sink, compatRecipeId("catalytic_cracker", name), input, outputs);
    }

    public static ResourceLocation registerCracking(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        return registerCracking(sink, compatRecipeId("catalytic_cracker", name), input, output1, output2);
    }

    public static JsonObject createFraction(FluidType input, HbmFluidStack[] outputs) {
        return createFraction(new HbmFluidStack(input, 100), outputs);
    }

    public static JsonObject createFraction(HbmFluidStack input, HbmFluidStack[] outputs) {
        HbmFluidStack[] safeOutputs = requireFluidOutputCount("fraction tower", outputs, 2);
        return createFraction(input, safeOutputs[0], safeOutputs[1]);
    }

    public static JsonObject createFraction(HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        requireFixedOilInput("fraction tower", input, 100);
        requireRealFluidOutputs("fraction tower", output1, output2);
        JsonObject json = oilProcessingBase(OilProcessingRecipe.Machine.FRACTION_TOWER, input);
        json.add("output1", fluid(output1));
        json.add("output2", fluid(output2));
        return json;
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, HbmFluidStack[] outputs) {
        return emit(sink, id, createFraction(input, outputs));
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return emit(sink, id, createFraction(input, outputs));
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack[] outputs) {
        return registerFraction(sink, compatRecipeId("fraction_tower", name), input, outputs);
    }

    public static ResourceLocation registerFraction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return registerFraction(sink, compatRecipeId("fraction_tower", name), input, outputs);
    }

    public static JsonObject createReforming(FluidType input, HbmFluidStack[] outputs) {
        return createReforming(new HbmFluidStack(input, 100), outputs);
    }

    public static JsonObject createReforming(HbmFluidStack input, HbmFluidStack[] outputs) {
        HbmFluidStack[] safeOutputs = requireFluidOutputCount("catalytic reformer", outputs, 3);
        return createReforming(input, safeOutputs[0], safeOutputs[1], safeOutputs[2]);
    }

    public static JsonObject createReforming(HbmFluidStack input, HbmFluidStack output1,
            HbmFluidStack output2, HbmFluidStack output3) {
        requireFixedOilInput("catalytic reformer", input, 100);
        requireRealFluidOutputs("catalytic reformer", output1, output2, output3);
        JsonObject json = oilProcessingBase(OilProcessingRecipe.Machine.CATALYTIC_REFORMER, input);
        json.add("output1", fluid(output1));
        json.add("output2", fluid(output2));
        json.add("output3", fluid(output3));
        return json;
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, HbmFluidStack[] outputs) {
        return emit(sink, id, createReforming(input, outputs));
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return emit(sink, id, createReforming(input, outputs));
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack[] outputs) {
        return registerReforming(sink, compatRecipeId("catalytic_reformer", name), input, outputs);
    }

    public static ResourceLocation registerReforming(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack[] outputs) {
        return registerReforming(sink, compatRecipeId("catalytic_reformer", name), input, outputs);
    }

    public static JsonObject createHydrotreating(FluidType input, HbmFluidStack hydrogen,
            HbmFluidStack[] outputs) {
        return createHydrotreating(new HbmFluidStack(input, 100), hydrogen, outputs);
    }

    public static JsonObject createHydrotreating(HbmFluidStack input, HbmFluidStack hydrogen,
            HbmFluidStack[] outputs) {
        HbmFluidStack[] safeOutputs = requireFluidOutputCount("hydrotreater", outputs, 2);
        return createHydrotreating(input, hydrogen, safeOutputs[0], safeOutputs[1]);
    }

    public static JsonObject createHydrotreating(HbmFluidStack input, HbmFluidStack hydrogen,
            HbmFluidStack output1, HbmFluidStack output2) {
        requireFixedOilInput("hydrotreater", input, 100);
        requireFluidOutput(hydrogen, "hydrotreater hydrogen input");
        requireRealFluidOutputs("hydrotreater", output1, output2);
        JsonObject json = oilProcessingBase(OilProcessingRecipe.Machine.HYDROTREATER, input);
        json.add("hydrogen", fluid(hydrogen));
        json.add("output1", fluid(output1));
        json.add("output2", fluid(output2));
        return json;
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, FluidType input, HbmFluidStack hydrogen, HbmFluidStack[] outputs) {
        return emit(sink, id, createHydrotreating(input, hydrogen, outputs));
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmFluidStack input, HbmFluidStack hydrogen, HbmFluidStack[] outputs) {
        return emit(sink, id, createHydrotreating(input, hydrogen, outputs));
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, HbmFluidStack hydrogen, HbmFluidStack[] outputs) {
        return registerHydrotreating(sink, compatRecipeId("hydrotreater", name), input, hydrogen, outputs);
    }

    public static ResourceLocation registerHydrotreating(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack hydrogen, HbmFluidStack[] outputs) {
        return registerHydrotreating(sink, compatRecipeId("hydrotreater", name), input, hydrogen, outputs);
    }

    public static JsonObject createSolidifying(HbmFluidStack input, ItemStack output) {
        requirePositiveOilInput("solidifier", input);
        requireItemOutput(output, "solidifier output");
        JsonObject json = oilProcessingBase(OilProcessingRecipe.Machine.SOLIDIFIER, input);
        json.add("output", HbmItemOutput.of(output).toJson());
        return json;
    }

    public static ResourceLocation registerSolidifying(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, ItemStack output) {
        return emit(sink, id, createSolidifying(input, output));
    }

    public static ResourceLocation registerSolidifying(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, ItemStack output) {
        return registerSolidifying(sink, compatRecipeId("solidifier", name), input, output);
    }

    public static JsonObject createCoker(HbmFluidStack input, ItemStack output, HbmFluidStack byproduct) {
        requirePositiveOilInput("coker", input);
        if ((output == null || output.isEmpty()) && (byproduct == null || byproduct.isEmpty())) {
            throw new IllegalArgumentException("HBM compat coker recipe must have an item output or fluid byproduct");
        }
        JsonObject json = oilProcessingBase(OilProcessingRecipe.Machine.COKER, input);
        if (output != null && !output.isEmpty()) {
            json.add("output", HbmItemOutput.of(output).toJson());
        }
        if (byproduct != null && !byproduct.isEmpty()) {
            json.add("byproduct", fluid(byproduct));
        }
        return json;
    }

    public static ResourceLocation registerCoker(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, ItemStack output, HbmFluidStack byproduct) {
        return emit(sink, id, createCoker(input, output, byproduct));
    }

    public static ResourceLocation registerCoker(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, ItemStack output, HbmFluidStack byproduct) {
        return registerCoker(sink, compatRecipeId("coker", name), input, output, byproduct);
    }

    public static JsonObject createCokerAuto(FluidType input, FluidType byproductType) {
        HbmFluidStack inputStack = new HbmFluidStack(input, cokerAutoAmount(input));
        HbmFluidStack byproduct = byproductType == null
                ? null
                : new HbmFluidStack(byproductType, Math.max(10, inputStack.amount() / 10));
        return createCoker(inputStack, new ItemStack(ModItems.legacyItem("coke_petroleum").get()), byproduct);
    }

    public static ResourceLocation registerCokerAuto(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            FluidType input, FluidType byproductType) {
        return emit(sink, id, createCokerAuto(input, byproductType));
    }

    public static ResourceLocation registerCokerAuto(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            FluidType input, FluidType byproductType) {
        return registerCokerAuto(sink, compatRecipeId("coker", name), input, byproductType);
    }

    public static JsonObject createExposureChamber(HbmIngredient particle, HbmIngredient ingredient,
            ItemStack output) {
        return createExposureChamber(particle, ingredient, output, -1);
    }

    public static JsonObject createExposureChamber(HbmIngredient particle, HbmIngredient ingredient,
            ItemStack output, int sourceOrder) {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(ingredient, "ingredient");
        requireItemOutput(output, "exposure chamber output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.EXPOSURE_CHAMBER.serializer().getId().toString());
        json.add("particle", particle.toJson());
        json.add("ingredient", ingredient.toJson());
        json.add("output", HbmItemOutput.of(output).toJson());
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient particle, HbmIngredient ingredient, ItemStack output) {
        return registerExposureChamber(sink, id, particle, ingredient, output, -1);
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient particle, HbmIngredient ingredient, ItemStack output,
            int sourceOrder) {
        return emit(sink, id, createExposureChamber(particle, ingredient, output, sourceOrder));
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient particle, HbmIngredient ingredient, ItemStack output) {
        return registerExposureChamber(sink, name, particle, ingredient, output, -1);
    }

    public static ResourceLocation registerExposureChamber(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient particle, HbmIngredient ingredient, ItemStack output, int sourceOrder) {
        return registerExposureChamber(sink, compatRecipeId("exposure_chamber", name), particle, ingredient, output,
                sourceOrder);
    }

    public static JsonObject createCyclotron(HbmIngredient particle, HbmIngredient input, ItemStack output,
            int antimatter, int sourceOrder) {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(input, "input");
        requireItemOutput(output, "cyclotron output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.CYCLOTRON.serializer().getId().toString());
        json.add("particle", particle.toJson());
        json.add("input", input.toJson());
        json.add("output", HbmItemOutput.of(output).toJson());
        json.addProperty("antimatter", Math.max(0, antimatter));
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerCyclotron(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient particle, HbmIngredient input, ItemStack output, int antimatter, int sourceOrder) {
        return emit(sink, id, createCyclotron(particle, input, output, antimatter, sourceOrder));
    }

    public static ResourceLocation registerCyclotron(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient particle, HbmIngredient input, ItemStack output, int antimatter, int sourceOrder) {
        return registerCyclotron(sink, compatRecipeId("cyclotron", name), particle, input, output, antimatter,
                sourceOrder);
    }

    public static JsonObject createParticleAccelerator(HbmIngredient input1, HbmIngredient input2, int momentum,
            ItemStack output1, ItemStack output2) {
        return createParticleAccelerator(input1, input2, momentum, output1, output2, -1);
    }

    public static JsonObject createParticleAccelerator(HbmIngredient input1, HbmIngredient input2, int momentum,
            ItemStack output1, ItemStack output2, int sourceOrder) {
        Objects.requireNonNull(input1, "input1");
        Objects.requireNonNull(input2, "input2");
        requireItemOutput(output1, "particle accelerator primary output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.PARTICLE_ACCELERATOR.serializer().getId().toString());
        JsonArray inputs = new JsonArray();
        inputs.add(input1.toJson());
        inputs.add(input2.toJson());
        json.add("inputs", inputs);
        json.addProperty("momentum", Math.max(0, momentum));
        JsonArray outputs = new JsonArray();
        outputs.add(HbmItemOutput.of(output1).toJson());
        if (output2 != null && !output2.isEmpty()) {
            outputs.add(HbmItemOutput.of(output2).toJson());
        }
        json.add("outputs", outputs);
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1,
            ItemStack output2) {
        return registerParticleAccelerator(sink, id, input1, input2, momentum, output1, output2, -1);
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1,
            ItemStack output2, int sourceOrder) {
        return emit(sink, id, createParticleAccelerator(input1, input2, momentum, output1, output2, sourceOrder));
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1, ItemStack output2) {
        return registerParticleAccelerator(sink, name, input1, input2, momentum, output1, output2, -1);
    }

    public static ResourceLocation registerParticleAccelerator(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input1, HbmIngredient input2, int momentum, ItemStack output1, ItemStack output2,
            int sourceOrder) {
        return registerParticleAccelerator(sink, compatRecipeId("particle_accelerator", name), input1, input2,
                momentum, output1, output2, sourceOrder);
    }

    public static JsonObject createAmmoPress(HbmIngredient[] inputItems, ItemStack output, int sourceOrder) {
        HbmIngredient[] inputs = inputItems == null ? new HbmIngredient[0] : Arrays.copyOf(inputItems, inputItems.length);
        if (inputs.length != 9) {
            throw new IllegalArgumentException("HBM compat ammo press recipe must have exactly 9 input slots");
        }
        requireItemOutput(output, "ammo press output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.AMMO_PRESS.serializer().getId().toString());
        JsonArray inputArray = new JsonArray();
        for (HbmIngredient input : inputs) {
            inputArray.add(input == null ? null : input.toJson());
        }
        json.add("input", inputArray);
        json.add("output", HbmItemOutput.of(output).toJson());
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerAmmoPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputItems, ItemStack output, int sourceOrder) {
        return emit(sink, id, createAmmoPress(inputItems, output, sourceOrder));
    }

    public static ResourceLocation registerAmmoPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputItems, ItemStack output, int sourceOrder) {
        return registerAmmoPress(sink, compatRecipeId("ammo_press", name), inputItems, output, sourceOrder);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output) {
        return createPedestal(inputItems, output, PedestalRecipe.ExtraCondition.NONE, 0, -1);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output, int condition) {
        return createPedestal(inputItems, output, pedestalExtra(condition), 0, -1);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output,
            PedestalRecipe.ExtraCondition extra) {
        return createPedestal(inputItems, output, extra, 0, -1);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, ItemStack output,
            PedestalRecipe.ExtraCondition extra, int set, int sourceOrder) {
        requireItemOutput(output, "pedestal output");
        return createPedestal(inputItems, HbmItemOutput.of(output), extra, set, sourceOrder);
    }

    public static JsonObject createPedestal(HbmIngredient[] inputItems, HbmItemOutput output,
            PedestalRecipe.ExtraCondition extra, int set, int sourceOrder) {
        HbmIngredient[] inputs = inputItems == null ? new HbmIngredient[0] : Arrays.copyOf(inputItems, inputItems.length);
        if (inputs.length != PedestalRecipe.SLOT_COUNT) {
            throw new IllegalArgumentException("HBM compat pedestal recipe must have exactly 9 input slots");
        }
        requireItemOutput(output, "pedestal output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.PEDESTAL.serializer().getId().toString());
        JsonArray inputArray = new JsonArray();
        for (HbmIngredient input : inputs) {
            inputArray.add(input == null ? null : input.toJson());
        }
        json.add("input", inputArray);
        json.add("output", output.toJson());
        json.addProperty("extra", (extra == null ? PedestalRecipe.ExtraCondition.NONE : extra).serializedName());
        if (set != 0) {
            json.addProperty("set", set);
        }
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputItems, ItemStack output) {
        return registerPedestal(sink, id, inputItems, output, PedestalRecipe.ExtraCondition.NONE, 0, -1);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputItems, ItemStack output, int condition) {
        return registerPedestal(sink, id, inputItems, output, pedestalExtra(condition), 0, -1);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient[] inputItems, ItemStack output, PedestalRecipe.ExtraCondition extra, int set,
            int sourceOrder) {
        return emit(sink, id, createPedestal(inputItems, output, extra, set, sourceOrder));
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputItems, ItemStack output) {
        return registerPedestal(sink, compatRecipeId("pedestal", name), inputItems, output);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputItems, ItemStack output, int condition) {
        return registerPedestal(sink, compatRecipeId("pedestal", name), inputItems, output, condition);
    }

    public static ResourceLocation registerPedestal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputItems, ItemStack output, PedestalRecipe.ExtraCondition extra, int set,
            int sourceOrder) {
        return registerPedestal(sink, compatRecipeId("pedestal", name), inputItems, output, extra, set, sourceOrder);
    }

    public static JsonObject createOutgasser(HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly) {
        return createOutgasser(input, solidOutput, fluidOutput, fusionOnly, -1);
    }

    public static JsonObject createOutgasser(HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly, int sourceOrder) {
        requireOutgasserEndpoints(input, solidOutput, fluidOutput);
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.OUTGASSER.serializer().getId().toString());
        json.add("input", input.toJson());
        if (solidOutput != null && !solidOutput.isEmpty()) {
            json.add("solid_output", HbmItemOutput.of(solidOutput).toJson());
        }
        if (fluidOutput != null && !fluidOutput.isEmpty()) {
            json.add("fluid_output", fluid(fluidOutput));
        }
        if (fusionOnly) {
            json.addProperty("fusion_only", true);
        }
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static JsonObject createOutgasser(HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput) {
        return createOutgasser(input, solidOutput, fluidOutput, false);
    }

    public static JsonObject createOutgasser(ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly) {
        return createOutgasser(HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly);
    }

    public static JsonObject createOutgasser(ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly, int sourceOrder) {
        return createOutgasser(HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly, sourceOrder);
    }

    public static JsonObject createOutgasser(TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly) {
        return createOutgasser(HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly);
    }

    public static JsonObject createOutgasser(TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput,
            boolean fusionOnly, int sourceOrder) {
        return createOutgasser(HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly, sourceOrder);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return registerOutgasser(sink, id, input, solidOutput, fluidOutput, fusionOnly, -1);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly,
            int sourceOrder) {
        return emit(sink, id, createOutgasser(input, solidOutput, fluidOutput, fusionOnly, sourceOrder));
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput) {
        return registerOutgasser(sink, id, input, solidOutput, fluidOutput, false);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return registerOutgasser(sink, compatRecipeId("outgasser", name), input, solidOutput, fluidOutput,
                fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly,
            int sourceOrder) {
        return registerOutgasser(sink, compatRecipeId("outgasser", name), input, solidOutput, fluidOutput,
                fusionOnly, sourceOrder);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack solidOutput, HbmFluidStack fluidOutput) {
        return registerOutgasser(sink, name, input, solidOutput, fluidOutput, false);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return registerOutgasser(sink, id, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly,
            int sourceOrder) {
        return registerOutgasser(sink, id, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly,
                sourceOrder);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return registerOutgasser(sink, id, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly,
            int sourceOrder) {
        return registerOutgasser(sink, id, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly,
                sourceOrder);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return registerOutgasser(sink, name, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly, int sourceOrder) {
        return registerOutgasser(sink, name, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly,
                sourceOrder);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly) {
        return registerOutgasser(sink, name, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly);
    }

    public static ResourceLocation registerOutgasser(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack solidOutput, HbmFluidStack fluidOutput, boolean fusionOnly,
            int sourceOrder) {
        return registerOutgasser(sink, name, HbmIngredient.of(input, 1), solidOutput, fluidOutput, fusionOnly,
                sourceOrder);
    }

    public static JsonObject createBreeder(HbmIngredient input, ItemStack output, int flux) {
        return createBreeder(input, output, flux, -1);
    }

    public static JsonObject createBreeder(HbmIngredient input, ItemStack output, int flux, int sourceOrder) {
        Objects.requireNonNull(input, "input");
        requireItemOutput(output, "breeder output");
        if (flux <= 0) {
            throw new IllegalArgumentException("HBM compat breeder recipe flux must be positive");
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.BREEDING_REACTOR.serializer().getId().toString());
        json.add("input", input.toJson());
        json.add("output", HbmItemOutput.of(output).toJson());
        json.addProperty("flux", flux);
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static JsonObject createBreeder(ItemLike input, ItemStack output, int flux) {
        return createBreeder(HbmIngredient.of(input, 1), output, flux);
    }

    public static JsonObject createBreeder(ItemLike input, ItemStack output, int flux, int sourceOrder) {
        return createBreeder(HbmIngredient.of(input, 1), output, flux, sourceOrder);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int flux) {
        return registerBreeder(sink, id, input, output, flux, -1);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int flux, int sourceOrder) {
        return emit(sink, id, createBreeder(input, output, flux, sourceOrder));
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int flux) {
        return registerBreeder(sink, name, input, output, flux, -1);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int flux, int sourceOrder) {
        return registerBreeder(sink, compatRecipeId("breeding_reactor", name), input, output, flux, sourceOrder);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int flux) {
        return registerBreeder(sink, id, input, output, flux, -1);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int flux, int sourceOrder) {
        return registerBreeder(sink, id, HbmIngredient.of(input, 1), output, flux, sourceOrder);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int flux) {
        return registerBreeder(sink, name, input, output, flux, -1);
    }

    public static ResourceLocation registerBreeder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int flux, int sourceOrder) {
        return registerBreeder(sink, compatRecipeId("breeding_reactor", name), input, output, flux, sourceOrder);
    }

    public static JsonObject createFuelPool(HbmIngredient input, ItemStack output) {
        return createFuelPool(input, output, -1);
    }

    public static JsonObject createFuelPool(HbmIngredient input, ItemStack output, int sourceOrder) {
        Objects.requireNonNull(input, "input");
        requireItemOutput(output, "fuel pool output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.FUEL_POOL.serializer().getId().toString());
        json.add("input", input.toJson());
        json.add("output", HbmItemOutput.of(output).toJson());
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static JsonObject createFuelPool(ItemStack input, ItemStack output) {
        return createFuelPool(input, output, -1);
    }

    public static JsonObject createFuelPool(ItemStack input, ItemStack output, int sourceOrder) {
        requireItemOutput(input, "fuel pool input");
        return createFuelPool(HbmIngredient.exact(input), output, sourceOrder);
    }

    public static JsonObject createFuelPool(ItemLike input, ItemStack output) {
        return createFuelPool(HbmIngredient.of(input, 1), output);
    }

    public static JsonObject createFuelPool(ItemLike input, ItemStack output, int sourceOrder) {
        return createFuelPool(HbmIngredient.of(input, 1), output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output) {
        return registerFuelPool(sink, id, input, output, -1);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int sourceOrder) {
        return emit(sink, id, createFuelPool(input, output, sourceOrder));
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output) {
        return registerFuelPool(sink, name, input, output, -1);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int sourceOrder) {
        return registerFuelPool(sink, compatRecipeId("fuel_pool", name), input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack input, ItemStack output) {
        return registerFuelPool(sink, id, input, output, -1);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemStack input, ItemStack output, int sourceOrder) {
        return registerFuelPool(sink, id, HbmIngredient.exact(input), output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack input, ItemStack output) {
        return registerFuelPool(sink, name, input, output, -1);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack input, ItemStack output, int sourceOrder) {
        return registerFuelPool(sink, compatRecipeId("fuel_pool", name), input, output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output) {
        return registerFuelPool(sink, id, input, output, -1);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int sourceOrder) {
        return registerFuelPool(sink, id, HbmIngredient.of(input, 1), output, sourceOrder);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output) {
        return registerFuelPool(sink, name, input, output, -1);
    }

    public static ResourceLocation registerFuelPool(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int sourceOrder) {
        return registerFuelPool(sink, compatRecipeId("fuel_pool", name), input, output, sourceOrder);
    }

    public static JsonObject createFusionReactor(String name, int duration, long power, long klystron,
            long plasma, double neutrons, HbmFluidStack[] inputFluids, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return createFusionReactor(compatRecipeId("fusion_reactor", name), name, duration, power, klystron, plasma,
                neutrons, inputFluids, outputItem, outputFluid);
    }

    public static JsonObject createFusionReactor(ResourceLocation id, String name, int duration, long power,
            long klystron, long plasma, double neutrons, HbmFluidStack[] inputFluids, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        JsonObject json = createGeneric(GenericMachineRecipe.Machine.FUSION_REACTOR, id, name, false, outputItem,
                duration, power, List.of(), firstFluids(inputFluids, 3), outputList(itemOutputOrNull(outputItem)),
                singleFluid(outputFluid));
        // 1.7.10 CompatRecipeRegistry#registerFusionReactor accepted a plasma parameter
        // but wrote klystron to both FusionRecipe input and output energy fields.
        addFusionExtra(json, klystron, klystron, neutrons);
        return json;
    }

    public static ResourceLocation registerFusionReactor(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, String name, int duration, long power, long klystron, long plasma,
            double neutrons, HbmFluidStack[] inputFluids, ItemStack outputItem, HbmFluidStack outputFluid) {
        return emit(sink, id, createFusionReactor(id, name, duration, power, klystron, plasma, neutrons,
                inputFluids, outputItem, outputFluid));
    }

    public static ResourceLocation registerFusionReactor(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            int duration, long power, long klystron, long plasma, double neutrons, HbmFluidStack[] inputFluids,
            ItemStack outputItem, HbmFluidStack outputFluid) {
        return registerFusionReactor(sink, compatRecipeId("fusion_reactor", name), name, duration, power, klystron,
                plasma, neutrons, inputFluids, outputItem, outputFluid);
    }

    public static JsonObject createCompressor(HbmFluidStack input, HbmFluidStack output, int duration) {
        requireFluidOutput(input, "compressor input");
        requireFluidOutput(output, "compressor output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.COMPRESSOR.serializer().getId().toString());
        json.add("input", fluid(input));
        json.add("output", fluid(output));
        json.addProperty("duration", Math.max(1, duration));
        return json;
    }

    public static ResourceLocation registerCompressor(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmFluidStack input, HbmFluidStack output, int duration) {
        return emit(sink, id, createCompressor(input, output, duration));
    }

    public static ResourceLocation registerCompressor(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack output, int duration) {
        return registerCompressor(sink, compatRecipeId("compressor", name), input, output, duration);
    }

    public static HbmFluidCompressorRecipes.Recipe registerCompressor(HbmFluidStack input, HbmFluidStack output,
            int duration) {
        return HbmFluidCompressorRecipes.register(input, output, duration);
    }

    public static List<HbmFluidCompressorRecipes.RecipeEntry> compressorRecipes() {
        return HbmFluidCompressorRecipes.recipes();
    }

    public static JsonObject createElectrolyzerFluid(HbmFluidStack input, HbmFluidStack output1,
            HbmFluidStack output2, ItemStack[] byproducts, int duration) {
        requireFluidOutput(input, "electrolyzer fluid input");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.ELECTROLYZER_FLUID.serializer().getId().toString());
        json.add("input", fluid(input));
        json.add("output1", output1 == null || output1.isEmpty()
                ? fluid(new HbmFluidStack(com.hbm.ntm.fluid.HbmFluids.NONE, 0))
                : fluid(output1));
        json.add("output2", output2 == null || output2.isEmpty()
                ? fluid(new HbmFluidStack(com.hbm.ntm.fluid.HbmFluids.NONE, 0))
                : fluid(output2));
        JsonArray byproductArray = new JsonArray();
        for (ItemStack byproduct : itemOutputList(byproducts).stream()
                .map(HbmItemOutput::representativeStack)
                .toList()) {
            byproductArray.add(HbmItemOutput.of(byproduct).toJson());
        }
        if (byproductArray.size() > 0) {
            json.add("byproducts", byproductArray);
        }
        json.addProperty("duration", Math.max(1, duration));
        return json;
    }

    public static ResourceLocation registerElectrolyzerFluid(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2,
            ItemStack[] byproducts, int duration) {
        return emit(sink, id, createElectrolyzerFluid(input, output1, output2, byproducts, duration));
    }

    public static ResourceLocation registerElectrolyzerFluid(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2, ItemStack[] byproducts,
            int duration) {
        return registerElectrolyzerFluid(sink, compatRecipeId("electrolyzer_fluid", name), input, output1,
                output2, byproducts, duration);
    }

    public static JsonObject createElectrolyzerMetal(HbmIngredient input, MaterialStack output1,
            MaterialStack output2, ItemStack[] byproducts, int duration) {
        Objects.requireNonNull(input, "electrolyzer metal input");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.ELECTROLYZER_METAL.serializer().getId().toString());
        json.add("input", input.toJson());
        if (output1 != null && !output1.isEmpty()) {
            json.add("output1", material(output1));
        }
        if (output2 != null && !output2.isEmpty()) {
            json.add("output2", material(output2));
        }
        JsonArray byproductArray = new JsonArray();
        for (ItemStack byproduct : itemOutputList(byproducts).stream()
                .map(HbmItemOutput::representativeStack)
                .toList()) {
            byproductArray.add(HbmItemOutput.of(byproduct).toJson());
        }
        if (byproductArray.size() > 0) {
            json.add("byproducts", byproductArray);
        }
        json.addProperty("duration", Math.max(1, duration));
        return json;
    }

    public static ResourceLocation registerElectrolyzerMetal(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input, MaterialStack output1, MaterialStack output2,
            ItemStack[] byproducts, int duration) {
        return emit(sink, id, createElectrolyzerMetal(input, output1, output2, byproducts, duration));
    }

    public static ResourceLocation registerElectrolyzerMetal(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, MaterialStack output1, MaterialStack output2, ItemStack[] byproducts,
            int duration) {
        return registerElectrolyzerMetal(sink, compatRecipeId("electrolyzer_metal", name), input, output1,
                output2, byproducts, duration);
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, List<HbmIngredient> inputs) {
        return createRotaryFurnace(output, duration, steam, fluid, inputs, -1);
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, List<HbmIngredient> inputs, int sourceOrder) {
        requireRotaryFurnaceShape(output, inputs);
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.ROTARY_FURNACE.serializer().getId().toString());
        json.add("output", material(output));
        json.addProperty("duration", Math.max(1, duration));
        json.addProperty("steam", Math.max(0, steam));
        if (fluid != null && !fluid.isEmpty()) {
            json.add("fluid", fluid(fluid));
        }
        JsonArray inputArray = new JsonArray();
        for (HbmIngredient input : nonNullList(inputs)) {
            inputArray.add(input.toJson());
        }
        if (inputArray.size() > 0) {
            json.add("inputs", inputArray);
        }
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, HbmIngredient[] inputs) {
        return createRotaryFurnace(output, duration, steam, fluid, list(inputs), -1);
    }

    public static JsonObject createRotaryFurnace(MaterialStack output, int duration, int steam,
            HbmFluidStack fluid, HbmIngredient[] inputs, int sourceOrder) {
        return createRotaryFurnace(output, duration, steam, fluid, list(inputs), sourceOrder);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            List<HbmIngredient> inputs) {
        return registerRotaryFurnace(sink, id, output, duration, steam, fluid, inputs, -1);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            List<HbmIngredient> inputs, int sourceOrder) {
        return emit(sink, id, createRotaryFurnace(output, duration, steam, fluid, inputs, sourceOrder));
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            HbmIngredient[] inputs) {
        return registerRotaryFurnace(sink, id, output, duration, steam, fluid, list(inputs), -1);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, MaterialStack output, int duration, int steam, HbmFluidStack fluid,
            HbmIngredient[] inputs, int sourceOrder) {
        return registerRotaryFurnace(sink, id, output, duration, steam, fluid, list(inputs), sourceOrder);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            MaterialStack output, int duration, int steam, HbmFluidStack fluid, HbmIngredient[] inputs) {
        return registerRotaryFurnace(sink, compatRecipeId("rotary_furnace", name), output, duration, steam,
                fluid, inputs, -1);
    }

    public static ResourceLocation registerRotaryFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            MaterialStack output, int duration, int steam, HbmFluidStack fluid, HbmIngredient[] inputs,
            int sourceOrder) {
        return registerRotaryFurnace(sink, compatRecipeId("rotary_furnace", name), output, duration, steam,
                fluid, inputs, sourceOrder);
    }

    public static JsonObject createCrucible(String internalName, String fallbackName, ItemStack icon,
            int frequency, MaterialStack[] input, MaterialStack[] output, int sourceOrder) {
        requireItemOutput(icon, "crucible icon");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.CRUCIBLE.serializer().getId().toString());
        json.addProperty("internal_name", internalName);
        if (fallbackName != null && !fallbackName.isBlank()) {
            json.addProperty("fallback_name", fallbackName);
        }
        json.add("icon", HbmItemOutput.of(icon).toJson());
        json.addProperty("frequency", Math.max(1, frequency));
        json.add("input", materials(list(input)));
        json.add("output", materials(list(output)));
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerCrucible(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String internalName, String fallbackName, ItemStack icon, int frequency, MaterialStack[] input,
            MaterialStack[] output, int sourceOrder) {
        return emit(sink, id, createCrucible(internalName, fallbackName, icon, frequency, input, output,
                sourceOrder));
    }

    public static ResourceLocation registerCrucible(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            String fallbackName, ItemStack icon, int frequency, MaterialStack[] input, MaterialStack[] output,
            int sourceOrder) {
        return registerCrucible(sink, compatRecipeId("crucible", name), name, fallbackName, icon, frequency,
                input, output, sourceOrder);
    }

    public static JsonObject createCrucibleSmelting(HbmIngredient input, MaterialStack[] output, int sourceOrder) {
        return createCrucibleSmelting(input, list(output), sourceOrder);
    }

    public static JsonObject createCrucibleSmelting(HbmIngredient input, List<MaterialStack> output,
            int sourceOrder) {
        Objects.requireNonNull(input, "crucible smelting input");
        JsonArray materialOutput = materials(output);
        if (materialOutput.size() == 0) {
            throw new IllegalArgumentException("HBM compat crucible smelting recipe must have material output");
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.CRUCIBLE_SMELTING.serializer().getId().toString());
        json.add("input", input.toJson());
        json.add("output", materialOutput);
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input, MaterialStack[] output, int sourceOrder) {
        return emit(sink, id, createCrucibleSmelting(input, output, sourceOrder));
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient input, List<MaterialStack> output, int sourceOrder) {
        return emit(sink, id, createCrucibleSmelting(input, output, sourceOrder));
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, MaterialStack[] output, int sourceOrder) {
        return registerCrucibleSmelting(sink, compatRecipeId("crucible_smelting", name), input, output,
                sourceOrder);
    }

    public static ResourceLocation registerCrucibleSmelting(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, List<MaterialStack> output, int sourceOrder) {
        return registerCrucibleSmelting(sink, compatRecipeId("crucible_smelting", name), input, output,
                sourceOrder);
    }

    public static JsonObject createArcWelder(String name, ItemStack output, int duration, long power,
            HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return createArcWelder(compatRecipeId("arc_welder", name), name, output, duration, power, inputFluid,
                inputItems);
    }

    public static JsonObject createArcWelder(ResourceLocation id, String name, ItemStack output, int duration,
            long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return createGeneric(GenericMachineRecipe.Machine.ARC_WELDER, id, name, false, output, duration, power,
                list(inputItems), singleFluid(inputFluid), itemOutputList(output),
                List.of());
    }

    public static JsonObject createArcWelder(String name, HbmItemOutput output, int duration, long power,
            HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return createArcWelder(compatRecipeId("arc_welder", name), name, output, duration, power, inputFluid,
                inputItems);
    }

    public static JsonObject createArcWelder(ResourceLocation id, String name, HbmItemOutput output, int duration,
            long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        requireItemOutput(output, "arc welder output");
        return createGeneric(GenericMachineRecipe.Machine.ARC_WELDER, id, name, false, output.representativeStack(),
                duration, power, list(inputItems), singleFluid(inputFluid), outputList(output), List.of());
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, ItemStack output, int duration, long power, HbmFluidStack inputFluid,
            HbmIngredient[] inputItems) {
        return emit(sink, id, createArcWelder(id, name, output, duration, power, inputFluid, inputItems));
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, HbmItemOutput output, int duration, long power, HbmFluidStack inputFluid,
            HbmIngredient[] inputItems) {
        return emit(sink, id, createArcWelder(id, name, output, duration, power, inputFluid, inputItems));
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return registerArcWelder(sink, compatRecipeId("arc_welder", name), name, output, duration, power,
                inputFluid, inputItems);
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmItemOutput output, int duration, long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return registerArcWelder(sink, compatRecipeId("arc_welder", name), name, output, duration, power,
                inputFluid, inputItems);
    }

    public static JsonObject createArcWelder(String name, ItemStack output, int duration, long power,
            HbmFluidStack inputFluid, ItemLike... inputItems) {
        return createArcWelder(name, output, duration, power, inputFluid, ingredients(inputItems));
    }

    public static JsonObject createArcWelder(String name, HbmItemOutput output, int duration, long power,
            HbmFluidStack inputFluid, ItemLike... inputItems) {
        return createArcWelder(name, output, duration, power, inputFluid, ingredients(inputItems));
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack inputFluid, ItemLike... inputItems) {
        return registerArcWelder(sink, name, output, duration, power, inputFluid, ingredients(inputItems));
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmItemOutput output, int duration, long power, HbmFluidStack inputFluid, ItemLike... inputItems) {
        return registerArcWelder(sink, name, output, duration, power, inputFluid, ingredients(inputItems));
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, ItemStack output, int duration, long power, HbmFluidStack inputFluid,
            ItemLike... inputItems) {
        return registerArcWelder(sink, id, name, output, duration, power, inputFluid, ingredients(inputItems));
    }

    public static ResourceLocation registerArcWelder(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, HbmItemOutput output, int duration, long power, HbmFluidStack inputFluid,
            ItemLike... inputItems) {
        return registerArcWelder(sink, id, name, output, duration, power, inputFluid, ingredients(inputItems));
    }

    public static JsonObject createArcFurnace(String name, ItemStack output, int duration, long power,
            HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return createArcFurnace(compatRecipeId("arc_furnace", name), name, output, duration, power, inputFluid,
                inputItems);
    }

    public static JsonObject createArcFurnace(ResourceLocation id, String name, ItemStack output, int duration,
            long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return createGeneric(GenericMachineRecipe.Machine.ARC_FURNACE, id, name, false, output, duration, power,
                list(inputItems), singleFluid(inputFluid), itemOutputList(output), List.of());
    }

    public static JsonObject createArcFurnace(String name, HbmItemOutput output, int duration, long power,
            HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return createArcFurnace(compatRecipeId("arc_furnace", name), name, output, duration, power, inputFluid,
                inputItems);
    }

    public static JsonObject createArcFurnace(ResourceLocation id, String name, HbmItemOutput output, int duration,
            long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        requireItemOutput(output, "arc furnace output");
        return createGeneric(GenericMachineRecipe.Machine.ARC_FURNACE, id, name, false, output.representativeStack(),
                duration, power, list(inputItems), singleFluid(inputFluid), outputList(output), List.of());
    }

    public static JsonObject createArcFurnace(ResourceLocation id, String name, HbmIngredient input,
            ItemStack output, MaterialStack materialOutput) {
        return createArcFurnace(id, name, input, output, materialOutput, 400, 1_000L);
    }

    public static JsonObject createArcFurnace(ResourceLocation id, String name, HbmIngredient input,
            ItemStack output, MaterialStack materialOutput, int duration, long power) {
        Objects.requireNonNull(input, "input");
        List<MaterialStack> materialOutputs = materialOutput == null || materialOutput.isEmpty()
                ? List.of()
                : List.of(materialOutput);
        List<HbmItemOutput> itemOutputs = output == null || output.isEmpty()
                ? List.of()
                : List.of(HbmItemOutput.of(output));
        if (itemOutputs.isEmpty() && materialOutputs.isEmpty()) {
            throw new IllegalArgumentException("HBM compat arc furnace recipe must have a solid or material output");
        }
        ItemStack icon = output == null ? ItemStack.EMPTY : output;
        JsonObject json = createGeneric(GenericMachineRecipe.Machine.ARC_FURNACE, id, name, false, icon,
                duration, power, List.of(input), List.of(), itemOutputs, List.of());
        JsonArray materialArray = materials(materialOutputs);
        if (materialArray.size() > 0) {
            json.add("arc_material_outputs", materialArray);
        }
        return json;
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, ItemStack output, int duration, long power, HbmFluidStack inputFluid,
            HbmIngredient[] inputItems) {
        return emit(sink, id, createArcFurnace(id, name, output, duration, power, inputFluid, inputItems));
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, HbmItemOutput output, int duration, long power, HbmFluidStack inputFluid,
            HbmIngredient[] inputItems) {
        return emit(sink, id, createArcFurnace(id, name, output, duration, power, inputFluid, inputItems));
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemStack output, int duration, long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return registerArcFurnace(sink, compatRecipeId("arc_furnace", name), name, output, duration, power,
                inputFluid, inputItems);
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmItemOutput output, int duration, long power, HbmFluidStack inputFluid, HbmIngredient[] inputItems) {
        return registerArcFurnace(sink, compatRecipeId("arc_furnace", name), name, output, duration, power,
                inputFluid, inputItems);
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, HbmIngredient input, ItemStack output, MaterialStack materialOutput) {
        return emit(sink, id, createArcFurnace(id, name, input, output, materialOutput));
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            String name, HbmIngredient input, ItemStack output, MaterialStack materialOutput, int duration,
            long power) {
        return emit(sink, id, createArcFurnace(id, name, input, output, materialOutput, duration, power));
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, MaterialStack materialOutput) {
        return registerArcFurnace(sink, compatRecipeId("arc_furnace", name), name, input, output, materialOutput);
    }

    public static ResourceLocation registerArcFurnace(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, MaterialStack materialOutput, int duration, long power) {
        return registerArcFurnace(sink, compatRecipeId("arc_furnace", name), name, input, output, materialOutput,
                duration, power);
    }

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, List<HbmIngredient> inputItems,
            List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems, List<HbmFluidStack> outputFluids) {
        return createGeneric(machine, id, name, named, icon, duration, power, inputItems, inputFluids, outputItems,
                outputFluids, -1);
    }

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, List<HbmIngredient> inputItems,
            List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems, List<HbmFluidStack> outputFluids,
            int sourceOrder) {
        List<HbmIngredient> safeInputItems = nonNullList(inputItems);
        List<HbmFluidStack> safeInputFluids = fluidList(inputFluids);
        List<HbmItemOutput> safeOutputItems = nonNullList(outputItems);
        List<HbmFluidStack> safeOutputFluids = fluidList(outputFluids);
        machine.validateRecipeLimits(id, safeInputItems.size(), safeInputFluids.size(), safeOutputItems.size(),
                safeOutputFluids.size());
        JsonObject json = new JsonObject();
        json.addProperty("type", machine.serializerId().toString());
        json.addProperty("internal_name", name == null || name.isBlank() ? id.toString() : name);
        if (named) {
            json.addProperty("custom_localization", true);
        }
        if (duration > 0) {
            json.addProperty("duration", duration);
        }
        if (power > 0L) {
            json.addProperty("power", power);
        }
        if (icon != null && !icon.isEmpty()) {
            json.add("icon", HbmItemOutput.of(icon).toJson());
        }
        json.add("input_items", itemInputs(safeInputItems));
        json.add("input_fluids", fluids(safeInputFluids));
        json.add("output_items", itemOutputs(safeOutputItems));
        json.add("output_fluids", fluids(safeOutputFluids));
        json.add("pools", new JsonArray());
        if (sourceOrder >= 0) {
            json.addProperty("source_order", sourceOrder);
        }
        return json;
    }

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return createGeneric(machine, id, name, named, icon, duration, power, list(inputItems), fluidList(inputFluids),
                outputList(outputItems), fluidList(outputFluids));
    }

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids, int sourceOrder) {
        return createGeneric(machine, id, name, named, icon, duration, power, list(inputItems), fluidList(inputFluids),
                outputList(outputItems), fluidList(outputFluids), sourceOrder);
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            GenericMachineRecipe.Machine machine, String name, boolean named, ItemStack icon, int duration, long power,
            List<HbmIngredient> inputItems, List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems,
            List<HbmFluidStack> outputFluids) {
        return emit(sink, id, createGeneric(machine, id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids));
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            GenericMachineRecipe.Machine machine, String name, boolean named, ItemStack icon, int duration, long power,
            List<HbmIngredient> inputItems, List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems,
            List<HbmFluidStack> outputFluids, int sourceOrder) {
        return emit(sink, id, createGeneric(machine, id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids, sourceOrder));
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            GenericMachineRecipe.Machine machine, String name, boolean named, ItemStack icon, int duration, long power,
            HbmIngredient[] inputItems, HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems,
            HbmFluidStack[] outputFluids) {
        return emit(sink, id, createGeneric(machine, id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids));
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            GenericMachineRecipe.Machine machine, String name, boolean named, ItemStack icon, int duration, long power,
            HbmIngredient[] inputItems, HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems,
            HbmFluidStack[] outputFluids, int sourceOrder) {
        return emit(sink, id, createGeneric(machine, id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids, sourceOrder));
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            GenericMachineRecipe.Machine machine, boolean named, ItemStack icon, int duration, long power,
            List<HbmIngredient> inputItems, List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems,
            List<HbmFluidStack> outputFluids) {
        return registerGeneric(sink, compatRecipeId(genericMachineFolder(machine), name), machine, name, named, icon,
                duration, power, inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            GenericMachineRecipe.Machine machine, boolean named, ItemStack icon, int duration, long power,
            List<HbmIngredient> inputItems, List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems,
            List<HbmFluidStack> outputFluids, int sourceOrder) {
        return registerGeneric(sink, compatRecipeId(genericMachineFolder(machine), name), machine, name, named, icon,
                duration, power, inputItems, inputFluids, outputItems, outputFluids, sourceOrder);
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            GenericMachineRecipe.Machine machine, boolean named, ItemStack icon, int duration, long power,
            HbmIngredient[] inputItems, HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems,
            HbmFluidStack[] outputFluids) {
        return registerGeneric(sink, compatRecipeId(genericMachineFolder(machine), name), machine, name, named, icon,
                duration, power, inputItems, inputFluids, outputItems, outputFluids);
    }

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            GenericMachineRecipe.Machine machine, boolean named, ItemStack icon, int duration, long power,
            HbmIngredient[] inputItems, HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems,
            HbmFluidStack[] outputFluids, int sourceOrder) {
        return registerGeneric(sink, compatRecipeId(genericMachineFolder(machine), name), machine, name, named, icon,
                duration, power, inputItems, inputFluids, outputItems, outputFluids, sourceOrder);
    }

    public static JsonObject createAnvilConstruction(List<HbmIngredient> inputs, List<HbmItemOutput> outputs,
            int tierLower, int tierUpper, AnvilConstructionRecipe.OverlayType overlay) {
        List<HbmIngredient> safeInputs = list(inputs);
        List<HbmItemOutput> safeOutputs = outputList(outputs);
        if (safeInputs.isEmpty()) {
            throw new IllegalArgumentException("HBM compat anvil construction recipe must have at least one input");
        }
        if (safeOutputs.isEmpty()) {
            throw new IllegalArgumentException("HBM compat anvil construction recipe must have at least one output");
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.ANVIL_CONSTRUCTION.serializer().getId().toString());
        json.add("inputs", itemInputs(safeInputs));
        json.add("outputs", itemOutputs(safeOutputs));
        json.addProperty("tier_lower", Math.max(0, tierLower));
        if (tierUpper >= 0) {
            json.addProperty("tier_upper", Math.max(Math.max(0, tierLower), tierUpper));
        }
        json.addProperty("overlay", (overlay == null ? AnvilConstructionRecipe.OverlayType.NONE : overlay)
                .name().toLowerCase(Locale.ROOT));
        return json;
    }

    public static JsonObject createAnvilConstruction(HbmIngredient[] inputs, HbmItemOutput[] outputs,
            int tierLower, int tierUpper, AnvilConstructionRecipe.OverlayType overlay) {
        return createAnvilConstruction(list(inputs), outputList(outputs), tierLower, tierUpper, overlay);
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int tierLower,
            int tierUpper, AnvilConstructionRecipe.OverlayType overlay) {
        return emit(sink, id, createAnvilConstruction(inputs, outputs, tierLower, tierUpper, overlay));
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, HbmIngredient[] inputs, HbmItemOutput[] outputs, int tierLower, int tierUpper,
            AnvilConstructionRecipe.OverlayType overlay) {
        return registerAnvilConstruction(sink, id, list(inputs), outputList(outputs), tierLower, tierUpper, overlay);
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            List<HbmIngredient> inputs, List<HbmItemOutput> outputs, int tierLower, int tierUpper,
            AnvilConstructionRecipe.OverlayType overlay) {
        return registerAnvilConstruction(sink, compatRecipeId("anvil_construction", name), inputs, outputs, tierLower,
                tierUpper, overlay);
    }

    public static ResourceLocation registerAnvilConstruction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient[] inputs, HbmItemOutput[] outputs, int tierLower, int tierUpper,
            AnvilConstructionRecipe.OverlayType overlay) {
        return registerAnvilConstruction(sink, compatRecipeId("anvil_construction", name), inputs, outputs, tierLower,
                tierUpper, overlay);
    }

    public static JsonObject createAnvilSmithing(HbmIngredient left, HbmIngredient right, HbmItemOutput output,
            int tier, boolean shapeless, int sourceOrder) {
        return createAnvilSmithing(left, right, output, tier, shapeless, sourceOrder,
                AnvilSmithingRecipe.Kind.STANDARD, null);
    }

    public static JsonObject createAnvilSmithing(HbmIngredient left, HbmIngredient right, HbmItemOutput output,
            int tier, boolean shapeless, int sourceOrder, AnvilSmithingRecipe.Kind kind, String moldPrefix) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(output, "output");
        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.ANVIL_SMITHING.serializer().getId().toString());
        json.add("left", left.toJson());
        json.add("right", right.toJson());
        json.add("output", output.toJson());
        json.addProperty("tier", Math.max(0, tier));
        if (shapeless) {
            json.addProperty("shapeless", true);
        }
        if (sourceOrder != Integer.MAX_VALUE) {
            json.addProperty("source_order", sourceOrder);
        }
        AnvilSmithingRecipe.Kind safeKind = kind == null ? AnvilSmithingRecipe.Kind.STANDARD : kind;
        if (safeKind != AnvilSmithingRecipe.Kind.STANDARD) {
            json.addProperty("kind", safeKind.jsonName());
        }
        if (moldPrefix != null && !moldPrefix.isBlank()) {
            json.addProperty("mold_prefix", moldPrefix);
        }
        return json;
    }

    public static JsonObject createAnvilSmithing(HbmIngredient left, HbmIngredient right, HbmItemOutput output,
            int tier, boolean shapeless) {
        return createAnvilSmithing(left, right, output, tier, shapeless, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerAnvilSmithing(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient left, HbmIngredient right, HbmItemOutput output, int tier, boolean shapeless,
            int sourceOrder) {
        return emit(sink, id, createAnvilSmithing(left, right, output, tier, shapeless, sourceOrder));
    }

    public static ResourceLocation registerAnvilSmithing(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient left, HbmIngredient right, HbmItemOutput output, int tier, boolean shapeless) {
        return registerAnvilSmithing(sink, id, left, right, output, tier, shapeless, Integer.MAX_VALUE);
    }

    public static ResourceLocation registerAnvilSmithing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient left, HbmIngredient right, HbmItemOutput output, int tier, boolean shapeless,
            int sourceOrder) {
        return registerAnvilSmithing(sink, compatRecipeId("anvil_smithing", name), left, right, output, tier,
                shapeless, sourceOrder);
    }

    public static ResourceLocation registerAnvilSmithing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient left, HbmIngredient right, HbmItemOutput output, int tier, boolean shapeless) {
        return registerAnvilSmithing(sink, compatRecipeId("anvil_smithing", name), left, right, output, tier,
                shapeless, Integer.MAX_VALUE);
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(LISTENERS.size(), lastInvokedListeners, lastFailedListeners, lastEmittedRecipes);
    }

    public static List<String> supportedRecipeFacades() {
        return recipeFacadeStatuses().stream()
                .filter(RecipeFacadeStatus::supported)
                .map(RecipeFacadeStatus::modernFacade)
                .distinct()
                .toList();
    }

    public static List<String> supportedLegacyRecipeFacades() {
        return recipeFacadeStatuses().stream()
                .filter(RecipeFacadeStatus::supported)
                .map(RecipeFacadeStatus::legacyMethod)
                .toList();
    }

    public static List<String> deferredLegacyRecipeFacades() {
        return recipeFacadeStatuses().stream()
                .filter(status -> !status.supported())
                .map(RecipeFacadeStatus::legacyMethod)
                .toList();
    }

    public static List<RecipeFacadeStatus> supportedRecipeFacadeStatuses() {
        return recipeFacadeStatuses().stream()
                .filter(RecipeFacadeStatus::supported)
                .toList();
    }

    public static List<RecipeFacadeStatus> deferredRecipeFacadeStatuses() {
        return recipeFacadeStatuses().stream()
                .filter(status -> !status.supported())
                .toList();
    }

    public static Optional<RecipeFacadeStatus> recipeFacadeStatus(String legacyMethodOrModernFacade) {
        if (legacyMethodOrModernFacade == null || legacyMethodOrModernFacade.isBlank()) {
            return Optional.empty();
        }
        String needle = legacyMethodOrModernFacade.toLowerCase(Locale.ROOT);
        return recipeFacadeStatuses().stream()
                .filter(status -> status.legacyMethod().equalsIgnoreCase(needle)
                        || status.modernFacade().equalsIgnoreCase(needle))
                .findFirst();
    }

    public static boolean isRecipeFacadeSupported(String legacyMethodOrModernFacade) {
        return recipeFacadeStatus(legacyMethodOrModernFacade)
                .map(RecipeFacadeStatus::supported)
                .orElse(false);
    }

    public static RecipeFacadeCoverage recipeFacadeCoverage() {
        List<RecipeFacadeStatus> statuses = recipeFacadeStatuses();
        int supported = (int) statuses.stream().filter(RecipeFacadeStatus::supported).count();
        return new RecipeFacadeCoverage(statuses.size(), supported, statuses.size() - supported);
    }

    public static List<RecipeFacadeStatus> recipeFacadeStatuses() {
        return List.of(
                supported("registerAssembler", "assembly_machine", "GenericMachineRecipe.Machine.ASSEMBLY_MACHINE"),
                supported("registerChemicalPlant", "chemical_plant", "GenericMachineRecipe.Machine.CHEMICAL_PLANT"),
                supported("registerPurex", "purex", "GenericMachineRecipe.Machine.PUREX"),
                supported("registerPrecass", "precass", "GenericMachineRecipe.Machine.PRECASS"),
                supported("registerPress", "press", "ModRecipes.PRESS"),
                supported("registerLiquefaction", "liquefaction", "ModRecipes.LIQUEFACTION"),
                supported("registerPyro", "pyro_oven", "ModRecipes.PYRO_OVEN"),
                supported("registerPyroAuto", "pyro_oven",
                        "ModRecipes.PYRO_OVEN datapack JSON sink with legacy solid-fuel auto formula"),
                supported("registerShredder", "shredder", "ModRecipes.SHREDDER"),
                supported("registerCentrifuge", "centrifuge", "ModRecipes.CENTRIFUGE"),
                supported("registerCrystallizer", "crystallizer", "ModRecipes.CRYSTALLIZER"),
                supported("registerItemProcessing", "item_processing_recipe_methods", "ItemProcessingRecipe JSON sink"),
                supported("registerGeneric", "recipe_sink_register_methods", "generic datapack JSON sink"),
                supported("registerSoldering", "soldering_station", "ModRecipes.SOLDERING_STATION"),
                supported("registerCombination", "combination_oven", "ModRecipes.COMBINATION_OVEN"),
                supported("registerCrucible", "crucible", "ModRecipes.CRUCIBLE datapack JSON sink"),
                supported("registerCrucibleSmelting", "crucible_smelting",
                        "ModRecipes.CRUCIBLE_SMELTING datapack JSON sink"),
                supported("registerBreeder", "breeding_reactor", "ModRecipes.BREEDING_REACTOR datapack JSON sink"),
                supported("registerCyclotron", "cyclotron", "ModRecipes.CYCLOTRON datapack JSON sink"),
                supported("registerFuelPool", "fuel_pool", "ModRecipes.FUEL_POOL datapack JSON sink"),
                supported("registerOutgasser", "outgasser", "ModRecipes.OUTGASSER"),
                supported("registerCompressor", "compressor",
                        "ModRecipes.COMPRESSOR datapack JSON sink for legacy direct calls; explicit runtime helper remains separate fallback"),
                supported("registerElectrolyzerFluid", "electrolyzer_fluid",
                        "ModRecipes.ELECTROLYZER_FLUID datapack JSON sink"),
                supported("registerElectrolyzerMetal", "electrolyzer_metal",
                        "ModRecipes.ELECTROLYZER_METAL datapack JSON sink"),
                supported("registerArcWelder", "arc_welder", "GenericMachineRecipe.Machine.ARC_WELDER"),
                supported("registerRotaryFurnace", "rotary_furnace", "ModRecipes.ROTARY_FURNACE"),
                supported("registerExposureChamber", "exposure_chamber", "ModRecipes.EXPOSURE_CHAMBER"),
                supported("registerMixer", "mixer", "ModRecipes.MIXER"),
                supported("registerCracking", "catalytic_cracker",
                        "ModRecipes.CATALYTIC_CRACKER datapack JSON sink"),
                supported("registerFraction", "fraction_tower",
                        "ModRecipes.FRACTION_TOWER datapack JSON sink"),
                supported("registerReforming", "catalytic_reformer",
                        "ModRecipes.CATALYTIC_REFORMER datapack JSON sink"),
                supported("registerHydrotreating", "hydrotreater",
                        "ModRecipes.HYDROTREATER datapack JSON sink"),
                supported("registerSolidifying", "solidifier",
                        "ModRecipes.SOLIDIFIER datapack JSON sink"),
                supported("registerCoker", "coker",
                        "ModRecipes.COKER datapack JSON sink"),
                supported("registerCokerAuto", "coker",
                        "ModRecipes.COKER datapack JSON sink with legacy auto fuel formula"),
                supported("registerFusionReactor", "fusion_reactor",
                        "GenericMachineRecipe.Machine.FUSION_REACTOR JSON sink with legacy klystron/output-energy quirk"),
                supported("registerParticleAccelerator", "particle_accelerator",
                        "ModRecipes.PARTICLE_ACCELERATOR datapack JSON sink"),
                supported("registerAmmoPress", "ammo_press", "ModRecipes.AMMO_PRESS"),
                supported("registerAnvilConstruction", "anvil_construction",
                        "ModRecipes.ANVIL_CONSTRUCTION JSON sink plus runtime helper; visible menu still separate"),
                supported("registerAnvilSmithing", "anvil_smithing",
                        "ModRecipes.ANVIL_SMITHING datapack JSON sink plus anvil menu runtime helper"),
                supported("registerPedestal", "pedestal", "ModRecipes.PEDESTAL datapack JSON sink"),
                supported("registerArcFurnace", "arc_furnace",
                        "ModRecipes.ARC_FURNACE solid and arc material output datapack JSON sink"),
                supported("registerAssembler(ItemStack,AStack[],int)", "legacy_noop",
                        "1.7.10 deprecated overload was an explicit no-op"),
                supported("registerAssembler(ItemStack,AStack[],int,Item...)", "legacy_noop",
                        "1.7.10 deprecated overload was an explicit no-op"),
                supported("registerChemplant", "legacy_noop",
                        "1.7.10 deprecated method was an explicit no-op"));
    }

    private static RecipeFacadeStatus supported(String legacyMethod, String modernFacade, String note) {
        return new RecipeFacadeStatus(legacyMethod, modernFacade, true, note);
    }

    private static RecipeFacadeStatus deferred(String legacyMethod, String modernFacade, String note) {
        return new RecipeFacadeStatus(legacyMethod, modernFacade, false, note);
    }

    public static ResourceLocation compatRecipeId(String recipeFolder, String name) {
        String folder = safePathPart(recipeFolder == null || recipeFolder.isBlank() ? "misc" : recipeFolder);
        String path = safePathPart(name == null || name.isBlank() ? "unnamed" : name);
        return new ResourceLocation(HbmNtm.MOD_ID, "compat/" + folder + "/" + path);
    }

    private static JsonArray itemInputs(List<HbmIngredient> inputs) {
        JsonArray array = new JsonArray();
        inputs.forEach(input -> array.add(input.toJson()));
        return array;
    }

    private static JsonArray itemOutputs(List<HbmItemOutput> outputs) {
        JsonArray array = new JsonArray();
        outputs.forEach(output -> array.add(output.toJson()));
        return array;
    }

    private static JsonArray materials(List<MaterialStack> stacks) {
        JsonArray array = new JsonArray();
        for (MaterialStack stack : nonNullList(stacks)) {
            if (stack != null && !stack.isEmpty()) {
                array.add(material(stack));
            }
        }
        return array;
    }

    private static JsonArray fluids(List<HbmFluidStack> stacks) {
        JsonArray array = new JsonArray();
        for (HbmFluidStack stack : stacks) {
            array.add(fluid(stack));
        }
        return array;
    }

    private static JsonObject oilProcessingBase(OilProcessingRecipe.Machine machine, HbmFluidStack input) {
        JsonObject json = new JsonObject();
        json.addProperty("type", oilProcessingSerializerId(machine).toString());
        json.add("input", fluid(input));
        return json;
    }

    private static ResourceLocation oilProcessingSerializerId(OilProcessingRecipe.Machine machine) {
        return switch (machine) {
            case REFINERY -> ModRecipes.REFINERY.serializer().getId();
            case CATALYTIC_CRACKER -> ModRecipes.CATALYTIC_CRACKER.serializer().getId();
            case CATALYTIC_REFORMER -> ModRecipes.CATALYTIC_REFORMER.serializer().getId();
            case VACUUM_DISTILL -> ModRecipes.VACUUM_DISTILL.serializer().getId();
            case FRACTION_TOWER -> ModRecipes.FRACTION_TOWER.serializer().getId();
            case HYDROTREATER -> ModRecipes.HYDROTREATER.serializer().getId();
            case SOLIDIFIER -> ModRecipes.SOLIDIFIER.serializer().getId();
            case COKER -> ModRecipes.COKER.serializer().getId();
        };
    }

    private static JsonObject fluid(HbmFluidStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", stack.type().getName());
        object.addProperty("amount", stack.amount());
        if (stack.pressure() != 0) {
            object.addProperty("pressure", stack.pressure());
        }
        return object;
    }

    private static JsonObject material(MaterialStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("material", stack.material.names[0]);
        object.addProperty("amount", stack.amount);
        return object;
    }

    private static ResourceLocation emit(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            JsonObject recipe) {
        Objects.requireNonNull(sink, "sink");
        Objects.requireNonNull(id, "id");
        sink.accept(id, recipe);
        return id;
    }

    private static String safePathPart(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        boolean lastWasSeparator = false;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            boolean allowed = c >= 'a' && c <= 'z'
                    || c >= '0' && c <= '9'
                    || c == '_'
                    || c == '-'
                    || c == '.';
            if (allowed) {
                builder.append(c);
                lastWasSeparator = false;
            } else if (!lastWasSeparator) {
                builder.append('_');
                lastWasSeparator = true;
            }
        }
        String result = builder.toString();
        while (result.startsWith("_")) {
            result = result.substring(1);
        }
        while (result.endsWith("_")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.isBlank() ? "unnamed" : result;
    }

    private static HbmItemOutput itemOutputOrNull(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : HbmItemOutput.of(stack);
    }

    private static PedestalRecipe.ExtraCondition pedestalExtra(int condition) {
        PedestalRecipe.ExtraCondition[] values = PedestalRecipe.ExtraCondition.values();
        return values[Math.floorMod(condition, values.length)];
    }

    private static void requireItemOutput(ItemStack stack, String name) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("HBM compat recipe " + name + " cannot be empty");
        }
    }

    private static void requireItemOutput(HbmItemOutput output, String name) {
        if (output == null) {
            throw new IllegalArgumentException("HBM compat recipe " + name + " cannot be empty");
        }
    }

    private static void requireFluidOutput(HbmFluidStack stack, String name) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("HBM compat recipe " + name + " cannot be empty");
        }
    }

    private static void requireFixedOilInput(String name, HbmFluidStack input, int amount) {
        requirePositiveOilInput(name, input);
        if (input.amount() != amount) {
            throw new IllegalArgumentException("HBM compat " + name + " input must be exactly " + amount
                    + "mB of a real fluid");
        }
    }

    private static void requirePositiveOilInput(String name, HbmFluidStack input) {
        if (input == null || input.isEmpty() || input.amount() <= 0) {
            throw new IllegalArgumentException("HBM compat " + name + " input must be a positive real fluid stack");
        }
    }

    private static HbmFluidStack[] requireFluidOutputCount(String name, HbmFluidStack[] outputs, int count) {
        HbmFluidStack[] safeOutputs = outputs == null ? new HbmFluidStack[0] : Arrays.copyOf(outputs, outputs.length);
        if (safeOutputs.length != count) {
            throw new IllegalArgumentException("HBM compat " + name + " recipe must have exactly " + count
                    + " fluid outputs");
        }
        return safeOutputs;
    }

    private static void requireRealFluidOutputs(String name, HbmFluidStack... outputs) {
        for (HbmFluidStack output : outputs) {
            if (output == null || output.isEmpty()) {
                throw new IllegalArgumentException("HBM compat " + name + " fluid outputs cannot be empty");
            }
        }
    }

    private static void requireCombinationOutput(HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        if (outputItem == null && (outputFluid == null || outputFluid.isEmpty())) {
            throw new IllegalArgumentException("HBM compat combination oven recipe must have an item or fluid output");
        }
    }

    private static void requirePyroEndpoints(HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        if (inputItem == null && (inputFluid == null || inputFluid.isEmpty())) {
            throw new IllegalArgumentException("HBM compat pyro oven recipe must have an item or fluid input");
        }
        if (outputItem == null && (outputFluid == null || outputFluid.isEmpty())) {
            throw new IllegalArgumentException("HBM compat pyro oven recipe must have an item or fluid output");
        }
    }

    private static void requireCrackingShape(HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2) {
        if (input == null || input.isEmpty() || input.amount() != 100) {
            throw new IllegalArgumentException("HBM compat cracking input must be exactly 100mB of a real fluid");
        }
        if (output1 == null || output2 == null) {
            throw new IllegalArgumentException("HBM compat cracking outputs cannot be null");
        }
        if (output1.isEmpty() && output2.isEmpty()) {
            throw new IllegalArgumentException("HBM compat cracking recipe must have at least one real output");
        }
    }

    private static int cokerAutoAmount(FluidType input) {
        long heat = Math.max(heatEnergy(input), combustionEnergy(input));
        if (heat <= 0L) {
            throw new IllegalArgumentException("HBM compat coker auto input has no flammable/combustible energy");
        }
        return autoAmount(820_000L, heat, 1.0D, 0);
    }

    private static int pyroAutoAmount(FluidType input, long tuPerFuel) {
        long heat = heatEnergy(input);
        if (heat <= 0L) {
            return 0;
        }
        return autoAmount(tuPerFuel, heat, 0.5D, 1);
    }

    private static int autoAmount(long tuPerFuel, long heatPerBucket, double penalty, int min) {
        int amount = (int) (tuPerFuel * 1_000L * penalty / heatPerBucket);
        if (amount > 10_000) {
            amount -= amount % 1_000;
        } else if (amount > 1_000) {
            amount -= amount % 100;
        } else if (amount > 100) {
            amount -= amount % 10;
        }
        return Math.max(amount, min);
    }

    private static long heatEnergy(FluidType type) {
        FlammableFluidTrait trait = type == null ? null : type.getTrait(FlammableFluidTrait.class);
        return trait == null ? 0L : trait.getHeatEnergyPerBucket();
    }

    private static long combustionEnergy(FluidType type) {
        CombustibleFluidTrait trait = type == null ? null : type.getTrait(CombustibleFluidTrait.class);
        return trait == null ? 0L : trait.getCombustionEnergyPerBucket();
    }

    private static void requireProcessingOutputs(ItemProcessingRecipe.Machine machine, List<HbmItemOutput> outputs) {
        int maxOutputs = switch (machine) {
            case SHREDDER, CRYSTALLIZER -> 1;
            case CENTRIFUGE -> 4;
        };
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("HBM compat " + itemProcessingFolder(machine)
                    + " recipe must have at least one output");
        }
        if (outputs.size() > maxOutputs) {
            throw new IllegalArgumentException("HBM compat " + itemProcessingFolder(machine)
                    + " recipe has too many outputs: " + outputs.size() + " > " + maxOutputs);
        }
    }

    private static void requireOutgasserEndpoints(HbmIngredient input, ItemStack solidOutput,
            HbmFluidStack fluidOutput) {
        Objects.requireNonNull(input, "input");
        boolean hasSolidOutput = solidOutput != null && !solidOutput.isEmpty();
        boolean hasFluidOutput = fluidOutput != null && !fluidOutput.isEmpty();
        if (!hasSolidOutput && !hasFluidOutput) {
            throw new IllegalArgumentException("HBM compat outgasser recipe must have a solid or fluid output");
        }
    }

    private static void requireBlastFurnaceShape(List<HbmIngredient> inputs, List<HbmItemOutput> outputs) {
        if (inputs.isEmpty() || inputs.size() > 2) {
            throw new IllegalArgumentException("HBM compat blast furnace recipe needs one or two item inputs");
        }
        if (outputs.isEmpty() || outputs.size() > 2) {
            throw new IllegalArgumentException("HBM compat blast furnace recipe needs one or two item outputs");
        }
    }

    private static void requireSolderingShape(List<HbmIngredient> toppings, List<HbmIngredient> pcb,
            List<HbmIngredient> solder) {
        if (toppings.size() > 3) {
            throw new IllegalArgumentException("HBM compat soldering station recipe has too many topping inputs");
        }
        if (pcb.size() > 2) {
            throw new IllegalArgumentException("HBM compat soldering station recipe has too many PCB inputs");
        }
        if (solder.size() > 1) {
            throw new IllegalArgumentException("HBM compat soldering station recipe has too many solder inputs");
        }
    }

    private static void requireRotaryFurnaceShape(MaterialStack output, List<HbmIngredient> inputs) {
        if (output == null || output.isEmpty()) {
            throw new IllegalArgumentException("HBM compat rotary furnace recipe must have a material output");
        }
        if (nonNullList(inputs).size() > 3) {
            throw new IllegalArgumentException("HBM compat rotary furnace recipe has too many item inputs");
        }
    }

    private static ResourceLocation itemProcessingSerializerId(ItemProcessingRecipe.Machine machine) {
        return switch (machine) {
            case SHREDDER -> ModRecipes.SHREDDER.serializer().getId();
            case CENTRIFUGE -> ModRecipes.CENTRIFUGE.serializer().getId();
            case CRYSTALLIZER -> ModRecipes.CRYSTALLIZER.serializer().getId();
        };
    }

    private static String itemProcessingFolder(ItemProcessingRecipe.Machine machine) {
        return switch (machine) {
            case SHREDDER -> "shredder";
            case CENTRIFUGE -> "centrifuge";
            case CRYSTALLIZER -> "crystallizer";
        };
    }

    private static String genericMachineFolder(GenericMachineRecipe.Machine machine) {
        Objects.requireNonNull(machine, "machine");
        return switch (machine) {
            case ASSEMBLY_MACHINE -> "assembly_machine";
            case CHEMICAL_PLANT -> "chemical_plant";
            case PUREX -> "purex";
            case PRECASS -> "precass";
            case ARC_WELDER -> "arc_welder";
            case ARC_FURNACE -> "arc_furnace";
            case FUSION_REACTOR -> "fusion_reactor";
            case PLASMA_FORGE -> "plasma_forge";
        };
    }

    private static List<HbmItemOutput> itemOutputList(ItemStack stack) {
        requireItemOutput(stack, "item processing output");
        return List.of(HbmItemOutput.of(stack));
    }

    private static List<HbmItemOutput> itemOutputList(ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) {
            return List.of();
        }
        return Arrays.stream(stacks)
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(HbmItemOutput::of)
                .toList();
    }

    private static List<HbmItemOutput> outputList(HbmItemOutput output) {
        return output == null ? List.of() : List.of(output);
    }

    private static List<HbmItemOutput> outputList(HbmItemOutput[] outputs) {
        if (outputs == null || outputs.length == 0) {
            return List.of();
        }
        return Arrays.stream(outputs)
                .filter(output -> output != null)
                .toList();
    }

    private static List<HbmItemOutput> outputList(List<HbmItemOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return List.of();
        }
        return outputs.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static HbmIngredient[] ingredients(ItemLike[] inputs) {
        if (inputs == null || inputs.length == 0) {
            return new HbmIngredient[0];
        }
        return Arrays.stream(inputs)
                .filter(Objects::nonNull)
                .map(input -> HbmIngredient.of(input, 1))
                .toArray(HbmIngredient[]::new);
    }

    private static <T> List<T> list(T[] array) {
        if (array == null || array.length == 0) {
            return List.of();
        }
        return Arrays.stream(array)
                .filter(entry -> entry != null)
                .toList();
    }

    private static <T> List<T> list(List<T> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        return entries.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static <T> List<T> nonNullList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(entry -> entry != null)
                .toList();
    }

    private static List<HbmFluidStack> singleFluid(HbmFluidStack stack) {
        return stack == null || stack.isEmpty() ? List.of() : List.of(stack);
    }

    private static List<HbmFluidStack> fluidList(List<HbmFluidStack> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .toList();
    }

    private static List<HbmFluidStack> fluidList(HbmFluidStack[] array) {
        if (array == null || array.length == 0) {
            return List.of();
        }
        return Arrays.stream(array)
                .filter(stack -> stack != null && !stack.isEmpty())
                .toList();
    }

    private static List<HbmFluidStack> firstFluids(HbmFluidStack[] array, int limit) {
        if (array == null || array.length == 0 || limit <= 0) {
            return List.of();
        }
        return Arrays.stream(array)
                .filter(stack -> stack != null && !stack.isEmpty())
                .limit(limit)
                .toList();
    }

    private static void addFusionExtra(JsonObject json, long ignitionTemp, long outputTemp, double outputFlux) {
        json.addProperty("ignitionTemp", ignitionTemp);
        json.addProperty("outputTemp", outputTemp);
        json.addProperty("outputFlux", outputFlux);
        json.addProperty("r", 1.0F);
        json.addProperty("g", 0.2F);
        json.addProperty("b", 0.6F);
    }

    public record Diagnostics(int listeners, int lastInvokedListeners, int lastFailedListeners,
                              int lastEmittedRecipes) {
        public String summary() {
            return "compat recipes listeners=" + listeners + " lastInvoked=" + lastInvokedListeners
                    + " lastFailed=" + lastFailedListeners + " lastEmittedRecipes=" + lastEmittedRecipes;
        }
    }

    public record RecipeFacadeStatus(String legacyMethod, String modernFacade, boolean supported, String note) {
        public String summary() {
            return legacyMethod + " -> " + modernFacade + " (" + (supported ? "supported" : "deferred")
                    + ": " + note + ")";
        }
    }

    public record RecipeFacadeCoverage(int total, int supported, int deferred) {
        public String summary() {
            return "recipe facades supported=" + supported + "/" + total + " deferred=" + deferred;
        }
    }

    @FunctionalInterface
    public interface RecipeRegisterListener extends com.hbm.ntm.api.recipe.RecipeRegisterListener {
    }

    @FunctionalInterface
    public interface RecipeSink extends com.hbm.ntm.api.recipe.RecipeSink {
    }

    private CompatRecipeRegistry() {
    }
}
