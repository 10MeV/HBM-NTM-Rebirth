package com.hbm.ntm.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ModRecipes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
                list(inputItems), list(inputFluids), list(outputItems), list(outputFluids));
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
                list(inputItems), list(inputFluids), list(outputItems), list(outputFluids));
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

    public static JsonObject createPyro(int duration, HbmIngredient inputItem, HbmFluidStack inputFluid,
            HbmItemOutput outputItem, HbmFluidStack outputFluid) {
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

    public static ResourceLocation registerGeneric(com.hbm.ntm.api.recipe.RecipeSink sink, ResourceLocation id,
            GenericMachineRecipe.Machine machine, String name, boolean named, ItemStack icon, int duration, long power,
            List<HbmIngredient> inputItems, List<HbmFluidStack> inputFluids, List<HbmItemOutput> outputItems,
            List<HbmFluidStack> outputFluids) {
        return emit(sink, id, createGeneric(machine, id, name, named, icon, duration, power, inputItems, inputFluids,
                outputItems, outputFluids));
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(LISTENERS.size(), lastInvokedListeners, lastFailedListeners, lastEmittedRecipes);
    }

    public static List<String> supportedRecipeFacades() {
        return List.of(
                "assembly_machine",
                "chemical_plant",
                "purex",
                "precass",
                "press",
                "liquefaction",
                "pyro_oven",
                "recipe_sink_register_methods");
    }

    public static List<String> deferredLegacyRecipeFacades() {
        return List.of(
                "blast_furnace",
                "shredder",
                "soldering",
                "combination",
                "crucible",
                "centrifuge",
                "crystallizer",
                "breeder",
                "cyclotron",
                "fuel_pool",
                "outgasser",
                "compressor",
                "electrolyzer_fluid",
                "electrolyzer_metal",
                "arc_welder",
                "rotary_furnace",
                "exposure_chamber",
                "fusion_reactor",
                "particle_accelerator",
                "ammo_press",
                "anvil_construction",
                "pedestal",
                "arc_furnace");
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

    private static void requireFluidOutput(HbmFluidStack stack, String name) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("HBM compat recipe " + name + " cannot be empty");
        }
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
    public interface RecipeRegisterListener extends com.hbm.ntm.api.recipe.RecipeRegisterListener {
    }

    @FunctionalInterface
    public interface RecipeSink extends com.hbm.ntm.api.recipe.RecipeSink {
    }

    private CompatRecipeRegistry() {
    }
}
