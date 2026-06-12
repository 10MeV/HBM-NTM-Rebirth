package com.hbm.ntm.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ModRecipes;
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
        JsonObject json = new JsonObject();
        Objects.requireNonNull(stamp, "stamp");
        Objects.requireNonNull(input, "input");
        requireItemOutput(output, "press output");
        json.addProperty("type", ModRecipes.PRESS.serializer().getId().toString());
        json.add("ingredient", input.toJson());
        json.addProperty("stamp", stamp.getSerializedName());
        json.add("result", HbmItemOutput.of(output).toJson());
        return json;
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return emit(sink, id, createPress(stamp, input, output));
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, Ingredient input, ItemStack output) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return createPress(stamp, Ingredient.of(input), output);
    }

    public static JsonObject createPress(ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return createPress(stamp, Ingredient.of(input), output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return registerPress(sink, id, stamp, Ingredient.of(input), output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return registerPress(sink, id, stamp, Ingredient.of(input), output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, ItemLike input, ItemStack output) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output);
    }

    public static ResourceLocation registerPress(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemPressStamp.StampType stamp, TagKey<Item> input, ItemStack output) {
        return registerPress(sink, compatRecipeId("press", name), stamp, input, output);
    }

    public static JsonObject createLiquefaction(Ingredient input, HbmFluidStack output) {
        JsonObject json = new JsonObject();
        Objects.requireNonNull(input, "input");
        requireFluidOutput(output, "liquefaction output");
        json.addProperty("type", ModRecipes.LIQUEFACTION.serializer().getId().toString());
        json.add("ingredient", input.toJson());
        json.add("output", fluid(output));
        return json;
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            Ingredient input, HbmFluidStack output) {
        return emit(sink, id, createLiquefaction(input, output));
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            Ingredient input, HbmFluidStack output) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output);
    }

    public static JsonObject createLiquefaction(ItemLike input, HbmFluidStack output) {
        return createLiquefaction(Ingredient.of(input), output);
    }

    public static JsonObject createLiquefaction(TagKey<Item> input, HbmFluidStack output) {
        return createLiquefaction(Ingredient.of(input), output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmFluidStack output) {
        return registerLiquefaction(sink, id, Ingredient.of(input), output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmFluidStack output) {
        return registerLiquefaction(sink, id, Ingredient.of(input), output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, HbmFluidStack output) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output);
    }

    public static ResourceLocation registerLiquefaction(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, HbmFluidStack output) {
        return registerLiquefaction(sink, compatRecipeId("liquefaction", name), input, output);
    }

    public static JsonObject createPyro(int duration, HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid) {
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
        return json;
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem,
            HbmFluidStack outputFluid) {
        return emit(sink, id, createPyro(duration, inputItem, inputFluid, outputItem, outputFluid));
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            HbmIngredient inputItem, HbmFluidStack inputFluid, HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid);
    }

    public static JsonObject createPyro(int duration, ItemLike inputItem, HbmFluidStack inputFluid,
            ItemStack outputItem, HbmFluidStack outputFluid) {
        return createPyro(duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1), inputFluid,
                itemOutputOrNull(outputItem), outputFluid);
    }

    public static JsonObject createPyro(int duration, TagKey<Item> inputItem, HbmFluidStack inputFluid,
            ItemStack outputItem, HbmFluidStack outputFluid) {
        return createPyro(duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1), inputFluid,
                itemOutputOrNull(outputItem), outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, ItemLike inputItem, HbmFluidStack inputFluid, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return registerPyro(sink, id, duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1),
                inputFluid, itemOutputOrNull(outputItem), outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            int duration, TagKey<Item> inputItem, HbmFluidStack inputFluid, ItemStack outputItem,
            HbmFluidStack outputFluid) {
        return registerPyro(sink, id, duration, inputItem == null ? null : HbmIngredient.of(inputItem, 1),
                inputFluid, itemOutputOrNull(outputItem), outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            ItemLike inputItem, HbmFluidStack inputFluid, ItemStack outputItem, HbmFluidStack outputFluid) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid);
    }

    public static ResourceLocation registerPyro(com.hbm.ntm.api.recipe.RecipeSink sink, String name, int duration,
            TagKey<Item> inputItem, HbmFluidStack inputFluid, ItemStack outputItem, HbmFluidStack outputFluid) {
        return registerPyro(sink, compatRecipeId("pyro_oven", name), duration, inputItem, inputFluid, outputItem,
                outputFluid);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
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
        return json;
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return createItemProcessing(machine, input, outputList(outputs), fluidInput, duration, productivity);
    }

    public static JsonObject createItemProcessing(ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return createItemProcessing(machine, input, itemOutputList(outputs), fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return emit(sink, id, createItemProcessing(machine, input, outputs, fluidInput, duration, productivity));
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            HbmItemOutput[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, id, machine, input, outputList(outputs), fluidInput, duration,
                productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink,
            ResourceLocation id, ItemProcessingRecipe.Machine machine, HbmIngredient input,
            ItemStack[] outputs, HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, id, machine, input, itemOutputList(outputs), fluidInput, duration,
                productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, List<HbmItemOutput> outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, HbmItemOutput[] outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity);
    }

    public static ResourceLocation registerItemProcessing(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemProcessingRecipe.Machine machine, HbmIngredient input, ItemStack[] outputs,
            HbmFluidStack fluidInput, int duration, float productivity) {
        return registerItemProcessing(sink, compatRecipeId(itemProcessingFolder(machine), name), machine, input,
                outputs, fluidInput, duration, productivity);
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
        return createItemProcessing(ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                itemOutputList(output), fluidInput, duration, productivity);
    }

    public static JsonObject createCrystallizer(HbmIngredient input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createItemProcessing(ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                outputList(output), fluidInput, duration, productivity);
    }

    public static JsonObject createCrystallizer(ItemLike input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static JsonObject createCrystallizer(ItemLike input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, ItemStack output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static JsonObject createCrystallizer(TagKey<Item> input, HbmItemOutput output, int duration,
            float productivity, HbmFluidStack fluidInput) {
        return createCrystallizer(HbmIngredient.of(input, 1), output, duration, productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                itemOutputList(output), fluidInput, duration, productivity);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerItemProcessing(sink, id, ItemProcessingRecipe.Machine.CRYSTALLIZER, input,
                outputList(output), fluidInput, duration, productivity);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, compatRecipeId("crystallizer", name), input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            HbmIngredient input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, compatRecipeId("crystallizer", name), input, output, duration,
                productivity, fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            ItemLike input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            TagKey<Item> input, HbmItemOutput output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, id, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            ItemLike input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, name, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
    }

    public static ResourceLocation registerCrystallizer(com.hbm.ntm.api.recipe.RecipeSink sink, String name,
            TagKey<Item> input, ItemStack output, int duration, float productivity, HbmFluidStack fluidInput) {
        return registerCrystallizer(sink, name, HbmIngredient.of(input, 1), output, duration, productivity,
                fluidInput);
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

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, List<HbmIngredient> inputItems,
            List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems, List<HbmFluidStack> outputFluids) {
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
        return json;
    }

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, HbmIngredient[] inputItems,
            HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return createGeneric(machine, id, name, named, icon, duration, power, list(inputItems), fluidList(inputFluids),
                outputList(outputItems), fluidList(outputFluids));
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
            HbmIngredient[] inputItems, HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems,
            HbmFluidStack[] outputFluids) {
        return emit(sink, id, createGeneric(machine, id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids));
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
            HbmIngredient[] inputItems, HbmFluidStack[] inputFluids, HbmItemOutput[] outputItems,
            HbmFluidStack[] outputFluids) {
        return registerGeneric(sink, compatRecipeId(genericMachineFolder(machine), name), machine, name, named, icon,
                duration, power, inputItems, inputFluids, outputItems, outputFluids);
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
                supported("registerShredder", "shredder", "ModRecipes.SHREDDER"),
                supported("registerCentrifuge", "centrifuge", "ModRecipes.CENTRIFUGE"),
                supported("registerCrystallizer", "crystallizer", "ModRecipes.CRYSTALLIZER"),
                supported("registerItemProcessing", "item_processing_recipe_methods", "ItemProcessingRecipe JSON sink"),
                supported("registerGeneric", "recipe_sink_register_methods", "generic datapack JSON sink"),
                deferred("registerBlastFurnace", "blast_furnace", "modern serializer not yet migrated"),
                deferred("registerSoldering", "soldering", "modern machine/serializer not yet migrated"),
                deferred("registerCombination", "combination", "modern serializer not yet migrated"),
                deferred("registerCrucible", "crucible", "material recipe serializer not yet migrated"),
                deferred("registerBreeder", "breeder", "reactor recipe serializer not yet migrated"),
                deferred("registerCyclotron", "cyclotron", "particle recipe serializer not yet migrated"),
                deferred("registerFuelPool", "fuel_pool", "reactor recipe serializer not yet migrated"),
                deferred("registerOutgasser", "outgasser", "modern serializer not yet migrated"),
                deferred("registerCompressor", "compressor", "modern serializer not yet migrated"),
                deferred("registerElectrolyzerFluid", "electrolyzer_fluid", "modern serializer not yet migrated"),
                deferred("registerElectrolyzerMetal", "electrolyzer_metal", "material recipe serializer not yet migrated"),
                supported("registerArcWelder", "arc_welder", "GenericMachineRecipe.Machine.ARC_WELDER"),
                deferred("registerRotaryFurnace", "rotary_furnace", "material recipe serializer not yet migrated"),
                deferred("registerExposureChamber", "exposure_chamber", "modern serializer not yet migrated"),
                deferred("registerFusionReactor", "fusion_reactor", "fusion recipe model not yet migrated"),
                deferred("registerParticleAccelerator", "particle_accelerator", "particle recipe model not yet migrated"),
                deferred("registerAmmoPress", "ammo_press", "modern serializer not yet migrated"),
                deferred("registerAnvilConstruction", "anvil_construction", "modern anvil recipe model not yet migrated"),
                deferred("registerPedestal", "pedestal", "modern pedestal recipe model not yet migrated"),
                deferred("registerArcFurnace", "arc_furnace", "modern serializer not yet migrated"));
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

    private static JsonArray fluids(List<HbmFluidStack> stacks) {
        JsonArray array = new JsonArray();
        for (HbmFluidStack stack : stacks) {
            array.add(fluid(stack));
        }
        return array;
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

    private static void requirePyroEndpoints(HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid) {
        if (inputItem == null && (inputFluid == null || inputFluid.isEmpty())) {
            throw new IllegalArgumentException("HBM compat pyro oven recipe must have an item or fluid input");
        }
        if (outputItem == null && (outputFluid == null || outputFluid.isEmpty())) {
            throw new IllegalArgumentException("HBM compat pyro oven recipe must have an item or fluid output");
        }
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
