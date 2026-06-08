package com.hbm.ntm.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Modernized compat recipe facade. It builds datapack-compatible recipe JSON
 * and records external listeners; it does not mutate the live RecipeManager.
 */
public final class CompatRecipeRegistry {
    private static final List<RecipeRegisterListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile int lastInvokedListeners;
    private static volatile int lastFailedListeners;
    private static volatile int lastEmittedRecipes;

    public static void registerRecipeRegisterListener(RecipeRegisterListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void emitRecipeRegisterListeners(RecipeSink sink) {
        lastInvokedListeners = 0;
        lastFailedListeners = 0;
        lastEmittedRecipes = 0;
        RecipeSink countingSink = (id, recipe) -> {
            sink.accept(id, recipe);
            lastEmittedRecipes++;
        };
        for (RecipeRegisterListener listener : LISTENERS) {
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

    public static JsonObject createChemicalPlant(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack[] inputFluids,
            HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return createGeneric(GenericMachineRecipe.Machine.CHEMICAL_PLANT, id, name, named, icon, duration, power,
                list(inputItems), list(inputFluids), list(outputItems), list(outputFluids));
    }

    public static JsonObject createPurex(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack[] inputFluids,
            HbmItemOutput[] outputItems, HbmFluidStack[] outputFluids) {
        return createGeneric(GenericMachineRecipe.Machine.PUREX, id, name, named, icon, duration, power,
                list(inputItems), list(inputFluids), list(outputItems), list(outputFluids));
    }

    public static JsonObject createPrecass(ResourceLocation id, String name, boolean named, ItemStack icon,
            int duration, long power, HbmIngredient[] inputItems, HbmFluidStack inputFluid,
            HbmItemOutput[] outputItems, HbmFluidStack outputFluid) {
        return createGeneric(GenericMachineRecipe.Machine.PRECASS, id, name, named, icon, duration, power,
                list(inputItems), singleFluid(inputFluid), list(outputItems), singleFluid(outputFluid));
    }

    public static JsonObject createGeneric(GenericMachineRecipe.Machine machine, ResourceLocation id, String name,
            boolean named, ItemStack icon, int duration, long power, List<HbmIngredient> inputItems,
            List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems, List<HbmFluidStack> outputFluids) {
        machine.validateRecipeLimits(id, inputItems.size(), inputFluids.size(), outputItems.size(), outputFluids.size());
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
        json.add("input_items", itemInputs(inputItems));
        json.add("input_fluids", fluids(inputFluids));
        json.add("output_items", itemOutputs(outputItems));
        json.add("output_fluids", fluids(outputFluids));
        json.add("pools", new JsonArray());
        return json;
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(LISTENERS.size(), lastInvokedListeners, lastFailedListeners, lastEmittedRecipes);
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
            JsonObject object = new JsonObject();
            object.addProperty("fluid", stack.type().getName());
            object.addProperty("amount", stack.amount());
            if (stack.pressure() != 0) {
                object.addProperty("pressure", stack.pressure());
            }
            array.add(object);
        }
        return array;
    }

    private static <T> List<T> list(T[] array) {
        if (array == null || array.length == 0) {
            return List.of();
        }
        return Arrays.stream(array)
                .filter(entry -> entry != null)
                .toList();
    }

    private static List<HbmFluidStack> singleFluid(HbmFluidStack stack) {
        return stack == null || stack.isEmpty() ? List.of() : List.of(stack);
    }

    public record Diagnostics(int listeners, int lastInvokedListeners, int lastFailedListeners,
                              int lastEmittedRecipes) {
        public String summary() {
            return "compat recipes listeners=" + listeners + " lastInvoked=" + lastInvokedListeners
                    + " lastFailed=" + lastFailedListeners + " lastEmittedRecipes=" + lastEmittedRecipes;
        }
    }

    @FunctionalInterface
    public interface RecipeRegisterListener {
        void registerRecipes(RecipeSink sink);
    }

    @FunctionalInterface
    public interface RecipeSink {
        void accept(ResourceLocation id, JsonObject recipe);
    }

    private CompatRecipeRegistry() {
    }
}
