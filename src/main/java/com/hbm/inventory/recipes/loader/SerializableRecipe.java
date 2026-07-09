package com.hbm.inventory.recipes.loader;

import api.hbm.recipe.IRecipeRegisterListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NBTStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.MainRegistry;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.LegacySerializableRecipeHandlers;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmRegistryUtil;
import com.hbm.util.Tuple.Pair;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Legacy 1.7.10 serializable recipe helper facade.
 *
 * <p>The old config-directory lifecycle is intentionally not restored. Use
 * datapack JSON and the modern import providers for shipped/default recipes.</p>
 */
@Deprecated(forRemoval = false)
public abstract class SerializableRecipe {
    public static final Gson gson = new Gson();
    public static List<SerializableRecipe> recipeHandlers = new ArrayList<>();
    public static List<IRecipeRegisterListener> additionalListeners = new ArrayList<>();
    public static Map<String, InputStream> recipeSyncHandlers = new HashMap<>();

    public boolean modified = false;

    public static void registerAllHandlers() {
        LegacySerializableRecipeHandlers.Coverage coverage = LegacySerializableRecipeHandlers.coverage();
        MainRegistry.logger.info("Legacy SerializableRecipe handler instantiation is disabled in the datapack-backed port ({} handlers tracked by metadata, {} generic importable, {} special-import, {} modern-only, {} unsupported).",
                coverage.totalHandlers(), coverage.genericSupported(), coverage.specialImporter(),
                coverage.modernSerializerOnly(), coverage.unsupported());
    }

    public static void initialize() {
        throw new UnsupportedOperationException("Legacy SerializableRecipe config loading is not restored; use datapack JSON/import providers instead.");
    }

    public static void receiveRecipes(String filename, byte[] data) {
        recipeSyncHandlers.put(filename, new ByteArrayInputStream(data));
    }

    public static void clearReceivedRecipes() {
        recipeSyncHandlers.clear();
    }

    public abstract String getFileName();

    public abstract Object getRecipeObject();

    public abstract void readRecipe(JsonElement recipe);

    public abstract void writeRecipe(Object recipe, JsonWriter writer) throws IOException;

    public abstract void registerDefaults();

    public abstract void deleteRecipes();

    public void registerPost() {
    }

    public String getComment() {
        return null;
    }

    public void writeTemplateFile(File template) {
        try {
            Object recipeObject = getRecipeObject();
            List<Object> recipeList = new ArrayList<>();
            if (recipeObject instanceof Collection<?> collection) {
                recipeList.addAll(collection);
            } else if (recipeObject instanceof HashMap<?, ?> map) {
                recipeList.addAll(map.entrySet());
            }

            if (recipeList.isEmpty() && !allowEmptyRecipeList()) {
                throw new IllegalStateException("Error while writing recipes for " + getClass().getSimpleName()
                        + ": Recipe list is either empty or in an unsupported format!");
            }

            JsonWriter writer = new JsonWriter(new FileWriter(template));
            writer.setIndent("  ");
            writer.beginObject();
            if (getComment() != null) {
                writer.name("comment").value(getComment());
            }
            writer.name("recipes").beginArray();
            for (Object recipe : recipeList) {
                writer.beginObject();
                writeRecipe(recipe, writer);
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean allowEmptyRecipeList() {
        return false;
    }

    public void readRecipeFile(File file) {
        try {
            readRecipeStream(new FileReader(file));
        } catch (FileNotFoundException ignored) {
        }
    }

    public void readRecipeStream(Reader reader) {
        JsonObject json = gson.fromJson(reader, JsonObject.class);
        JsonArray recipes = json.get("recipes").getAsJsonArray();
        for (JsonElement recipe : recipes) {
            if (recipe != null) {
                readRecipe(recipe);
            }
        }
    }

    public static AStack readAStack(JsonArray array) {
        try {
            return readAStackStrict(array);
        } catch (Exception ignored) {
        }
        logReadError("stack", array);
        return new ComparableStack(ModItems.NOTHING.get());
    }

    private static AStack readAStackStrict(JsonArray array) {
        String type = array.get(0).getAsString();
        int stacksize = array.size() > 2 ? array.get(2).getAsInt() : 1;
        if ("dict".equals(type)) {
            return new OreDictStack(array.get(1).getAsString(), stacksize);
        }
        if ("item".equals(type) || "nbt".equals(type)) {
            ResourceLocation legacyId = normalizeLegacyId(array.get(1).getAsString());
            int meta = array.size() > 3 ? array.get(3).getAsInt() : 0;
            boolean nbt = "nbt".equals(type);
            if (nbt) {
                ItemStack stack = readLegacyItemStack(legacyId, stacksize, 0);
                if (array.size() > 4) {
                    CompoundTag tag = parseLegacyCompoundNbt(array.get(4).getAsString(), "legacy NBTStack");
                    if (tag != null) {
                        stack.setTag(tag);
                    }
                }
                return new NBTStack(stack);
            }
            if (meta == HbmIngredient.WILDCARD_META) {
                return new IngredientStack(HbmIngredient.legacyWildcard(legacyId, stacksize));
            }
            ItemStack stack = readLegacyItemStack(legacyId, stacksize, meta);
            return new ComparableStack(stack);
        }
        throw new JsonSyntaxException("Unsupported legacy AStack type: " + type);
    }

    public static AStack[] readAStackArray(JsonArray array) {
        try {
            AStack[] items = new AStack[array.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = readAStack((JsonArray) array.get(i));
            }
            return items;
        } catch (Exception ignored) {
        }
        logReadError("stack", array);
        return new AStack[0];
    }

    public static void writeAStack(AStack astack, JsonWriter writer) throws IOException {
        if (astack instanceof IngredientStack ingredientStack) {
            writeJsonArray(LegacyGenericRecipeFormat.writeLegacyAStack(ingredientStack.ingredient), writer);
            return;
        }

        writer.beginArray();
        writer.setIndent("");
        if (astack instanceof NBTStack nbtStack) {
            ItemStack stack = nbtStack.toStack();
            writer.value(nbtStack.nbt != null ? "nbt" : "item");
            writer.value(itemName(stack));
            if (nbtStack.stacksize != 1 || nbtStack.meta > 0 || nbtStack.nbt != null) {
                writer.value(nbtStack.stacksize);
            }
            if (nbtStack.meta > 0 || nbtStack.nbt != null) {
                writer.value(nbtStack.meta);
            }
            if (nbtStack.nbt != null) {
                writer.value(nbtStack.nbt.toString());
            }
        } else if (astack instanceof ComparableStack comp) {
            ItemStack stack = comp.toStack();
            writer.value("item");
            writer.value(itemName(stack));
            if (comp.stacksize != 1 || comp.meta > 0) {
                writer.value(comp.stacksize);
            }
            if (comp.meta > 0) {
                writer.value(comp.meta);
            }
        } else if (astack instanceof OreDictStack ore) {
            writer.value("dict");
            writer.value(ore.name);
            if (ore.stacksize != 1) {
                writer.value(ore.stacksize);
            }
        }
        writer.endArray();
        writer.setIndent("  ");
    }

    public static ItemStack readItemStack(JsonArray array) {
        try {
            return readItemStackStrict(array);
        } catch (Exception ignored) {
        }
        logReadError("stack", array, " - defaulting to NOTHING item!");
        return nothingStack();
    }

    private static ItemStack readItemStackStrict(JsonArray array) {
        ResourceLocation legacyId = normalizeLegacyId(array.get(0).getAsString());
        int stacksize = array.size() > 1 ? array.get(1).getAsInt() : 1;
        int meta = array.size() > 2 ? array.get(2).getAsInt() : 0;
        ItemStack stack = readLegacyItemStack(legacyId, stacksize, meta);
        if (array.size() > 3) {
            CompoundTag tag = parseLegacyCompoundNbtLenient(array.get(3).getAsString());
            if (tag != null) {
                stack.setTag(tag);
            }
        }
        return stack;
    }

    public static Pair<ItemStack, Float> readItemStackChance(JsonArray array) {
        try {
            float chance = array.get(array.size() - 1).getAsFloat();
            JsonArray stackArray = new JsonArray();
            for (int i = 0; i < array.size() - 1; i++) {
                stackArray.add(array.get(i));
            }
            return new Pair<>(readItemStackStrict(stackArray), chance);
        } catch (Exception ignored) {
        }
        logReadError("stack", array, " - defaulting to NOTHING item!");
        return new Pair<>(nothingStack(), 1F);
    }

    public static ItemStack[] readItemStackArray(JsonArray array) {
        try {
            ItemStack[] items = new ItemStack[array.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = readItemStack((JsonArray) array.get(i));
            }
            return items;
        } catch (Exception ignored) {
        }
        logReadError("stack", array);
        return new ItemStack[0];
    }

    public static Pair<ItemStack, Float>[] readItemStackArrayChance(JsonArray array) {
        try {
            Pair<ItemStack, Float>[] items = new Pair[array.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = readItemStackChance((JsonArray) array.get(i));
            }
            return items;
        } catch (Exception ignored) {
        }
        logReadError("stack", array);
        return new Pair[0];
    }

    public static void writeItemStack(ItemStack stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        writer.value(itemName(stack));
        if (stack.getCount() != 1 || stack.getDamageValue() != 0 || stack.hasTag()) {
            writer.value(stack.getCount());
        }
        if (stack.getDamageValue() != 0 || stack.hasTag()) {
            writer.value(stack.getDamageValue());
        }
        if (stack.hasTag()) {
            writer.value(stack.getTag().toString());
        }
        writer.endArray();
        writer.setIndent("  ");
    }

    public static void writeItemStackChance(Pair<ItemStack, Float> stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        ItemStack item = stack.getKey();
        writer.value(itemName(item));
        if (item.getCount() != 1 || item.getDamageValue() != 0 || item.hasTag()) {
            writer.value(item.getCount());
        }
        if (item.getDamageValue() != 0 || item.hasTag()) {
            writer.value(item.getDamageValue());
        }
        if (item.hasTag()) {
            writer.value(item.getTag().toString());
        }
        writer.value(stack.value);
        writer.endArray();
        writer.setIndent("  ");
    }

    public static FluidStack readFluidStack(JsonArray array) {
        try {
            return readFluidStackStrict(array);
        } catch (Exception ignored) {
        }
        logReadError("fluid", array);
        return new FluidStack(Fluids.NONE, 0);
    }

    private static FluidStack readFluidStackStrict(JsonArray array) {
        FluidType type = Fluids.fromName(array.get(0).getAsString());
        int fill = array.get(1).getAsInt();
        int pressure = array.size() < 3 ? 0 : array.get(2).getAsInt();
        return new FluidStack(type, fill, pressure);
    }

    public static FluidStack[] readFluidArray(JsonArray array) {
        try {
            FluidStack[] fluids = new FluidStack[array.size()];
            for (int i = 0; i < fluids.length; i++) {
                fluids[i] = readFluidStack((JsonArray) array.get(i));
            }
            return fluids;
        } catch (Exception ignored) {
        }
        logReadError("fluid", array);
        return new FluidStack[0];
    }

    public static void writeFluidStack(FluidStack stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        writer.value(stack.type.getName());
        writer.value(stack.fill);
        if (stack.pressure != 0) {
            writer.value(stack.pressure);
        }
        writer.endArray();
        writer.setIndent("  ");
    }

    public static boolean matchesIngredients(ItemStack[] inputs, AStack[] recipe) {
        List<AStack> recipeList = new ArrayList<>();
        for (AStack ingredient : recipe) {
            recipeList.add(ingredient);
        }

        for (ItemStack inputStack : inputs) {
            if (inputStack != null) {
                boolean hasMatch = false;
                Iterator<AStack> iterator = recipeList.iterator();
                while (iterator.hasNext()) {
                    AStack recipeStack = iterator.next();
                    if (recipeStack.matchesRecipe(inputStack, true) && inputStack.getCount() >= recipeStack.stacksize) {
                        hasMatch = true;
                        recipeList.remove(recipeStack);
                        break;
                    }
                }
                if (!hasMatch) {
                    return false;
                }
            }
        }
        return recipeList.isEmpty();
    }

    private static ItemStack readLegacyItemStack(ResourceLocation legacyId, int count, int legacyMeta) {
        if (LegacyMetaItemMappings.item(legacyId, legacyMeta).isPresent()) {
            return LegacyMetaItemMappings.stackPreservingCount(legacyId, legacyMeta, count).orElseThrow();
        }
        if (legacyMeta != 0) {
            throw new JsonSyntaxException("Missing legacy item meta mapping: " + legacyId + " meta " + legacyMeta);
        }

        Item item = HbmRegistryUtil.item(modernIdForRegistryLookup(legacyId))
                .or(() -> isHbmLegacyNamespace(legacyId)
                        ? legacyItem(legacyId.getPath())
                        : java.util.Optional.empty())
                .orElseThrow(() -> new JsonSyntaxException("Unknown legacy item: " + legacyId));
        return new ItemStack(item, count);
    }

    private static ItemStack nothingStack() {
        return new ItemStack(ModItems.NOTHING.get());
    }

    private static void logReadError(String kind, JsonArray array) {
        logReadError(kind, array, "");
    }

    private static void logReadError(String kind, JsonArray array, String suffix) {
        MainRegistry.logger.error("Error reading {} array {}{}", kind, String.valueOf(array), suffix);
    }

    private static java.util.Optional<Item> legacyItem(String path) {
        var item = ModItems.legacyItem(path);
        return item == null ? java.util.Optional.empty() : java.util.Optional.of(item.get());
    }

    private static ResourceLocation normalizeLegacyId(String id) {
        ResourceLocation parsed = id.contains(":") ? new ResourceLocation(id) : new ResourceLocation("hbm", id);
        return isHbmLegacyNamespace(parsed) ? new ResourceLocation("hbm", parsed.getPath()) : parsed;
    }

    private static ResourceLocation modernIdForRegistryLookup(ResourceLocation legacyId) {
        return isHbmLegacyNamespace(legacyId) ? new ResourceLocation(HbmNtm.MOD_ID, legacyId.getPath()) : legacyId;
    }

    private static boolean isHbmLegacyNamespace(ResourceLocation id) {
        return "hbm".equals(id.getNamespace()) || HbmNtm.MOD_ID.equals(id.getNamespace());
    }

    private static CompoundTag parseLegacyCompoundNbt(String nbt, String name) {
        try {
            StringReader reader = new StringReader(nbt);
            Tag tag = new TagParser(reader).readValue();
            reader.skipWhitespace();
            if (reader.canRead()) {
                throw TagParser.ERROR_TRAILING_DATA.createWithContext(reader);
            }
            return tag instanceof CompoundTag compound ? compound : null;
        } catch (CommandSyntaxException exception) {
            throw new JsonSyntaxException("Invalid NBT in " + name + ": " + exception.getMessage(), exception);
        }
    }

    private static CompoundTag parseLegacyCompoundNbtLenient(String nbt) {
        try {
            return parseLegacyCompoundNbt(nbt, "legacy ItemStack");
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

    private static String itemName(ItemStack stack) {
        ResourceLocation name = HbmRegistryUtil.itemKey(stack.getItem());
        if (name == null) {
            throw new JsonSyntaxException("Cannot serialize unregistered item stack: " + stack);
        }
        return legacyItemName(name).toString();
    }

    private static ResourceLocation legacyItemName(ResourceLocation name) {
        return HbmNtm.MOD_ID.equals(name.getNamespace()) ? new ResourceLocation("hbm", name.getPath()) : name;
    }

    private static void writeJsonArray(JsonArray array, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isString()) {
                    writer.value(element.getAsString());
                } else if (element.getAsJsonPrimitive().isNumber()) {
                    writer.value(element.getAsNumber());
                } else if (element.getAsJsonPrimitive().isBoolean()) {
                    writer.value(element.getAsBoolean());
                }
            } else if (element.isJsonArray()) {
                writeJsonArray(element.getAsJsonArray(), writer);
            } else {
                writer.jsonValue(element.toString());
            }
        }
        writer.endArray();
        writer.setIndent("  ");
    }

    private static final class IngredientStack extends AStack {
        private final HbmIngredient ingredient;

        private IngredientStack(HbmIngredient ingredient) {
            this.ingredient = ingredient;
            this.stacksize = ingredient.count();
        }

        @Override
        public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {
            return ingredient.test(stack, ignoreSize);
        }

        @Override
        public AStack copy() {
            return new IngredientStack(ingredient);
        }

        @Override
        public AStack copy(int stacksize) {
            List<ItemStack> display = ingredient.displayStacks();
            if (display.size() == 1) {
                ItemStack stack = display.get(0);
                stack.setCount(stacksize);
                return ingredient.hasPartialNbt() ? new NBTStack(stack) : new ComparableStack(stack);
            }
            return new IngredientStack(new HbmIngredient(ingredient.ingredient(), stacksize, ingredient.exactStack(),
                    ingredient.partialNbt(), ingredient.legacyId(), ingredient.legacyMeta(), ingredient.legacyWildcard(),
                    ingredient.legacyOreName(), ingredient.fluidContainerType(), ingredient.fluidContainerAmount()));
        }

        @Override
        public List<ItemStack> extractForNEI() {
            return ingredient.displayStacks();
        }

        @Override
        public int compareTo(AStack other) {
            return toString().compareTo(String.valueOf(other));
        }

        @Override
        public String toString() {
            return ingredient.diagnosticName();
        }
    }
}
