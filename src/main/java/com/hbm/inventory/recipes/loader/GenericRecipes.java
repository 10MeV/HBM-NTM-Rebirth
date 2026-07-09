package com.hbm.inventory.recipes.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.util.ItemStackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Legacy 1.7.10 generic recipe list facade.
 *
 * <p>This class intentionally remains an in-memory DTO/import helper. Shipped
 * defaults and live machine runtime stay in modern datapack recipes.</p>
 */
@Deprecated(forRemoval = false)
public abstract class GenericRecipes<T extends GenericRecipe> extends SerializableRecipe {
    public static final Random RNG = new Random();

    public static final String POOL_PREFIX_ALT = "alt.";
    public static final String POOL_PREFIX_DISCOVER = "discover.";
    public static final String POOL_PREFIX_SECRET = "secret.";
    public static final String POOL_PREFIX_528 = "528.";

    public List<T> recipeOrderedList = new ArrayList<>();
    public HashMap<String, T> recipeNameMap = new HashMap<>();

    public static HashMap<String, List<String>> blueprintPools = new HashMap<>();
    public static HashMap<String, GenericRecipe> pooledBlueprints = new HashMap<>();

    public HashMap<String, List<GenericRecipe>> autoSwitchGroups = new HashMap<>();

    public abstract int inputItemLimit();

    public abstract int inputFluidLimit();

    public abstract int outputItemLimit();

    public abstract int outputFluidLimit();

    public boolean hasDuration() {
        return true;
    }

    public boolean hasPower() {
        return true;
    }

    public static void addToPool(String pool, GenericRecipe recipe) {
        List<String> list = blueprintPools.get(pool);
        if (list == null) {
            list = new ArrayList<>();
            blueprintPools.put(pool, list);
        }
        list.add(recipe.name);
        pooledBlueprints.put(recipe.name, recipe);
    }

    public void addToGroup(String group, GenericRecipe recipe) {
        List<GenericRecipe> list = autoSwitchGroups.get(group);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(recipe);
        autoSwitchGroups.put(group, list);
    }

    public static void clearPools() {
        blueprintPools.clear();
        pooledBlueprints.clear();
    }

    @Override
    public Object getRecipeObject() {
        return recipeOrderedList;
    }

    @Override
    public void deleteRecipes() {
        recipeOrderedList.clear();
        recipeNameMap.clear();
        autoSwitchGroups.clear();
    }

    public void register(T recipe) {
        recipeOrderedList.add(recipe);
        if (recipeNameMap.containsKey(recipe.name)) {
            throw new IllegalStateException("Recipe " + recipe.name + " has been registered with a duplicate ID!");
        }
        recipeNameMap.put(recipe.name, recipe);
    }

    @Override
    public void readRecipe(JsonElement element) {
        JsonObject obj = (JsonObject) element;
        T recipe = instantiateRecipe(obj.get("name").getAsString());

        if (inputItemLimit() > 0 && obj.has("inputItem")) {
            recipe.inputItem = readAStackArray(obj.get("inputItem").getAsJsonArray());
        }
        if (inputFluidLimit() > 0 && obj.has("inputFluid")) {
            recipe.inputFluid = readFluidArray(obj.get("inputFluid").getAsJsonArray());
        }
        if (outputItemLimit() > 0 && obj.has("outputItem")) {
            recipe.outputItem = readOutputArray(obj.get("outputItem").getAsJsonArray());
        }
        if (outputFluidLimit() > 0 && obj.has("outputFluid")) {
            recipe.outputFluid = readFluidArray(obj.get("outputFluid").getAsJsonArray());
        }
        if (hasDuration()) {
            recipe.setDuration(obj.get("duration").getAsInt());
        }
        if (hasPower()) {
            recipe.setPower(obj.get("power").getAsLong());
        }
        if (obj.has("icon")) {
            recipe.setIcon(readItemStack(obj.get("icon").getAsJsonArray()));
        }
        if (obj.has("named") && obj.get("named").getAsBoolean()) {
            recipe.setNamed();
        }
        if (obj.has("blueprintpool")) {
            recipe.setPoolsAllow528(obj.get("blueprintpool").getAsString().split(":"));
        }
        if (obj.has("nameWrapper")) {
            recipe.setNameWrapper(obj.get("nameWrapper").getAsString());
        }
        if (obj.has("autoSwitchGroup")) {
            recipe.setGroup(obj.get("autoSwitchGroup").getAsString(), this);
        }

        readExtraData(element, recipe);
        register(recipe);
    }

    public abstract T instantiateRecipe(String name);

    public void readExtraData(JsonElement element, T recipe) {
    }

    @Override
    public void writeRecipe(Object recipeObject, JsonWriter writer) throws IOException {
        T recipe = (T) recipeObject;
        writer.name("name").value(recipe.name);

        if (inputItemLimit() > 0 && recipe.inputItem != null) {
            writer.name("inputItem").beginArray();
            for (AStack stack : recipe.inputItem) {
                writeAStack(stack, writer);
            }
            writer.endArray();
        }
        if (inputFluidLimit() > 0 && recipe.inputFluid != null) {
            writer.name("inputFluid").beginArray();
            for (FluidStack stack : recipe.inputFluid) {
                writeFluidStack(stack, writer);
            }
            writer.endArray();
        }
        if (outputItemLimit() > 0 && recipe.outputItem != null) {
            writer.name("outputItem").beginArray();
            for (IOutput stack : recipe.outputItem) {
                stack.serialize(writer);
            }
            writer.endArray();
        }
        if (outputFluidLimit() > 0 && recipe.outputFluid != null) {
            writer.name("outputFluid").beginArray();
            for (FluidStack stack : recipe.outputFluid) {
                writeFluidStack(stack, writer);
            }
            writer.endArray();
        }
        if (hasDuration()) {
            writer.name("duration").value(recipe.duration);
        }
        if (hasPower()) {
            writer.name("power").value(recipe.power);
        }
        if (recipe.writeIcon) {
            writer.name("icon");
            writeItemStack(recipe.icon, writer);
        }
        if (recipe.customLocalization) {
            writer.name("named").value(true);
        }
        if (recipe.nameWrapper != null) {
            writer.name("nameWrapper").value(recipe.nameWrapper);
        }
        if (recipe.blueprintPools != null && recipe.blueprintPools.length > 0) {
            writer.name("blueprintpool").value(String.join(":", recipe.blueprintPools));
        }
        if (recipe.autoSwitchGroup != null) {
            writer.name("autoSwitchGroup").value(recipe.autoSwitchGroup);
        }

        writeExtraData(recipe, writer);
    }

    public void writeExtraData(T recipe, JsonWriter writer) throws IOException {
    }

    public IOutput[] readOutputArray(JsonArray array) {
        IOutput[] output = new IOutput[array.size()];
        int index = 0;
        for (JsonElement element : array) {
            JsonArray arrayElement = element.getAsJsonArray();
            String type = arrayElement.get(0).getAsString();
            if ("single".equals(type)) {
                ChanceOutput chance = new ChanceOutput();
                chance.deserialize(arrayElement);
                output[index] = chance;
            } else if ("multi".equals(type)) {
                ChanceOutputMulti multi = new ChanceOutputMulti();
                multi.deserialize(arrayElement);
                output[index] = multi;
            } else {
                throw new IllegalArgumentException("Invalid IOutput type '" + type + "', expected 'single' or 'multi' for recipe " + array);
            }
            index++;
        }
        return output;
    }

    public interface IOutput {
        boolean possibleMultiOutput();

        ItemStack collapse();

        ItemStack getSingle();

        ItemStack[] getAllPossibilities();

        void serialize(JsonWriter writer) throws IOException;

        void deserialize(JsonArray array);

        String[] getLabel();
    }

    public static class ChanceOutput implements IOutput {
        public ItemStack stack;
        public float chance = 1F;
        public int itemWeight = 0;

        public ChanceOutput() {
        }

        public ChanceOutput(ItemStack stack) {
            this(stack, 1F, 0);
        }

        public ChanceOutput(ItemStack stack, int weight) {
            this(stack, 1F, weight);
        }

        public ChanceOutput(ItemStack stack, float chance) {
            this(stack, chance, 0);
        }

        public ChanceOutput(ItemStack stack, float chance, int weight) {
            this.stack = stack;
            this.chance = chance;
            this.itemWeight = weight;
        }

        @Override
        public ItemStack collapse() {
            if (chance >= 1F) {
                return getSingle();
            }
            int finalSize = 0;
            for (int i = 0; i < stack.getCount(); i++) {
                if (RNG.nextFloat() <= chance) {
                    finalSize++;
                }
            }
            if (finalSize <= 0) {
                return null;
            }
            ItemStack finalStack = getSingle();
            finalStack.setCount(finalSize);
            return finalStack;
        }

        @Override
        public ItemStack getSingle() {
            return stack.copy();
        }

        @Override
        public boolean possibleMultiOutput() {
            return false;
        }

        @Override
        public ItemStack[] getAllPossibilities() {
            return new ItemStack[] { chance >= 1F ? getSingle()
                    : ItemStackUtil.addTooltipToStack(getSingle(), ChatFormatting.RED + "" + (int) (chance * 1000) / 10F + "%") };
        }

        @Override
        public void serialize(JsonWriter writer) throws IOException {
            boolean standardStack = chance >= 1 && itemWeight == 0;
            writer.beginArray();
            writer.setIndent("");
            if (itemWeight == 0) {
                writer.value("single");
            }
            SerializableRecipe.writeItemStack(stack, writer);
            writer.setIndent("");
            if (!standardStack) {
                writer.value(chance);
                if (itemWeight > 0) {
                    writer.value(itemWeight);
                }
            }
            writer.endArray();
            writer.setIndent("  ");
        }

        @Override
        public void deserialize(JsonArray array) {
            if (array.get(0).isJsonPrimitive()) {
                stack = SerializableRecipe.readItemStack(array.get(1).getAsJsonArray());
                if (array.size() > 2) {
                    chance = array.get(2).getAsFloat();
                }
            } else {
                stack = SerializableRecipe.readItemStack(array.get(0).getAsJsonArray());
                if (array.size() > 1) {
                    chance = array.get(1).getAsFloat();
                }
                if (array.size() > 2) {
                    itemWeight = array.get(2).getAsInt();
                }
            }
        }

        @Override
        public String[] getLabel() {
            return new String[] { ChatFormatting.GRAY + "" + stack.getCount() + "x " + stack.getHoverName().getString()
                    + (chance >= 1 ? "" : " (" + (int) (chance * 1000) / 10F + "%)") };
        }
    }

    public static class ChanceOutputMulti implements IOutput {
        public List<ChanceOutput> pool = new ArrayList<>();

        public ChanceOutputMulti(ChanceOutput... out) {
            for (ChanceOutput output : out) {
                pool.add(output);
            }
        }

        @Override
        public ItemStack collapse() {
            return chooseWeighted(pool).collapse();
        }

        @Override
        public boolean possibleMultiOutput() {
            return pool.size() > 1;
        }

        @Override
        public ItemStack getSingle() {
            return possibleMultiOutput() ? null : pool.get(0).getSingle();
        }

        @Override
        public ItemStack[] getAllPossibilities() {
            ItemStack[] outputs = new ItemStack[pool.size()];
            int totalWeight = totalWeight(pool);
            for (int i = 0; i < outputs.length; i++) {
                ChanceOutput out = pool.get(i);
                float chance = (float) out.itemWeight / (float) totalWeight;
                outputs[i] = chance >= 1 ? out.getAllPossibilities()[0]
                        : ItemStackUtil.addTooltipToStack(out.getAllPossibilities()[0],
                        ChatFormatting.RED + "" + (int) (chance * 1000) / 10F + "%");
            }
            return outputs;
        }

        @Override
        public void serialize(JsonWriter writer) throws IOException {
            writer.beginArray();
            writer.value("multi");
            for (ChanceOutput output : pool) {
                output.serialize(writer);
            }
            writer.endArray();
        }

        @Override
        public void deserialize(JsonArray array) {
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    continue;
                }
                ChanceOutput output = new ChanceOutput();
                output.deserialize(element.getAsJsonArray());
                pool.add(output);
            }
        }

        @Override
        public String[] getLabel() {
            String[] label = new String[pool.size() + 1];
            label[0] = "One of:";
            int totalWeight = totalWeight(pool);
            for (int i = 1; i < label.length; i++) {
                ChanceOutput output = pool.get(i - 1);
                float chance = (float) output.itemWeight / (float) totalWeight * output.chance;
                label[i] = "  " + ChatFormatting.GRAY + output.stack.getCount() + "x "
                        + output.stack.getHoverName().getString() + " (" + (int) (chance * 1000F) / 10F + "%)";
            }
            return label;
        }

        private static ChanceOutput chooseWeighted(List<ChanceOutput> pool) {
            int total = totalWeight(pool);
            if (total <= 0) {
                throw new IllegalArgumentException();
            }
            int value = RNG.nextInt(total);
            for (ChanceOutput output : pool) {
                value -= output.itemWeight;
                if (value < 0) {
                    return output;
                }
            }
            return null;
        }

        private static int totalWeight(List<ChanceOutput> pool) {
            int total = 0;
            for (ChanceOutput output : pool) {
                total += output.itemWeight;
            }
            return total;
        }
    }

    public Map<String, T> recipeNameMapView() {
        return Map.copyOf(recipeNameMap);
    }
}
