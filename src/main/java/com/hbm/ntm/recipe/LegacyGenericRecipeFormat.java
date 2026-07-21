package com.hbm.ntm.recipe;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LegacyGenericRecipeFormat {
    private LegacyGenericRecipeFormat() {
    }

    public static String internalName(ResourceLocation id, JsonObject json) {
        return getString(json, "internal_name", getString(json, "name", id.toString()));
    }

    public static List<HbmIngredient> readItemInputs(JsonObject json) {
        if (json.has("input_items")) {
            return json.getAsJsonArray("input_items").asList().stream()
                    .map(element -> HbmIngredient.fromJson(element.getAsJsonObject()))
                    .toList();
        }
        if (json.has("inputItem")) {
            return readLegacyAStackArray(json.getAsJsonArray("inputItem"));
        }
        return List.of();
    }

    public static List<HbmItemOutput> readItemOutputs(JsonObject json) {
        if (json.has("output_items")) {
            return json.getAsJsonArray("output_items").asList().stream()
                    .map(element -> HbmItemOutput.fromJson(element.getAsJsonObject()))
                    .toList();
        }
        if (json.has("outputItem")) {
            return readLegacyOutputArray(json.getAsJsonArray("outputItem"));
        }
        return List.of();
    }

    public static List<HbmFluidStack> readFluidInputs(JsonObject json) {
        if (json.has("input_fluids")) {
            return readModernFluidStacks(json.getAsJsonArray("input_fluids"));
        }
        if (json.has("inputFluid")) {
            return readLegacyFluidArray(json.getAsJsonArray("inputFluid"));
        }
        return List.of();
    }

    public static List<HbmFluidStack> readFluidOutputs(JsonObject json) {
        if (json.has("output_fluids")) {
            return readModernFluidStacks(json.getAsJsonArray("output_fluids"));
        }
        if (json.has("outputFluid")) {
            return readLegacyFluidArray(json.getAsJsonArray("outputFluid"));
        }
        return List.of();
    }

    public static List<String> readPools(JsonObject json) {
        if (json.has("pools")) {
            return json.getAsJsonArray("pools").asList().stream()
                    .map(JsonElement::getAsString)
                    .toList();
        }
        if (json.has("blueprintpool")) {
            String rawPools = json.get("blueprintpool").getAsString();
            if (rawPools.isBlank()) {
                return List.of();
            }
            return List.of(rawPools.split(":"));
        }
        return List.of();
    }

    public static String readAutoSwitchGroup(JsonObject json) {
        return getString(json, "auto_switch_group", getString(json, "autoSwitchGroup", null));
    }

    public static ItemStack readIcon(JsonObject json) {
        if (json.has("icon")) {
            JsonElement icon = json.get("icon");
            if (icon.isJsonObject()) {
                return HbmItemOutput.fromJson(icon.getAsJsonObject()).representativeStack();
            }
            if (icon.isJsonArray()) {
                return readLegacyItemStack(icon.getAsJsonArray());
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean readCustomLocalization(JsonObject json) {
        return (json.has("custom_localization") && json.get("custom_localization").getAsBoolean())
                || (json.has("named") && json.get("named").getAsBoolean());
    }

    public static String readNameWrapper(JsonObject json) {
        return getString(json, "name_wrapper", getString(json, "nameWrapper", null));
    }

    public static int readSourceOrder(JsonObject json) {
        if (json.has("source_order")) {
            return json.get("source_order").getAsInt();
        }
        if (json.has("legacy_order")) {
            return json.get("legacy_order").getAsInt();
        }
        return GenericMachineRecipe.UNSPECIFIED_SOURCE_ORDER;
    }

    public static JsonArray writeLegacyAStack(HbmIngredient ingredient) {
        JsonArray array = new JsonArray();
        if (ingredient.legacyOreName() != null) {
            array.add("dict");
            array.add(ingredient.legacyOreName());
            if (ingredient.count() != 1) {
                array.add(ingredient.count());
            }
            return array;
        }

        if (ingredient.legacyId() != null) {
            array.add(ingredient.hasPartialNbt() ? "nbt" : "item");
            array.add(legacyItemId(ingredient.legacyId()).toString());
            if (ingredient.count() != 1 || ingredient.legacyMeta() > 0 || ingredient.hasPartialNbt() || ingredient.legacyWildcard()) {
                array.add(ingredient.count());
            }
            if (ingredient.legacyMeta() > 0 || ingredient.hasPartialNbt() || ingredient.legacyWildcard()) {
                array.add(ingredient.legacyMeta());
            }
            if (ingredient.hasPartialNbt()) {
                array.add(ingredient.partialNbt().toString());
            }
            return array;
        }

        if (ingredient.isTagIngredient()) {
            throw new JsonSyntaxException("Cannot write tag ingredient as legacy AStack without legacy_ore provenance: "
                    + ingredient.diagnosticName());
        }
        if (ingredient.fluidContainerType() != null) {
            throw new JsonSyntaxException("Cannot write fluid-container ingredient as legacy AStack without legacy ore provenance: "
                    + ingredient.diagnosticName());
        }

        Optional<ItemStack> stack = representativeInputStack(ingredient);
        if (stack.isPresent()) {
            array.add(ingredient.hasPartialNbt() ? "nbt" : "item");
            writeLegacyItemStackBody(array, stack.get(), ingredient.count(),
                    ingredient.hasPartialNbt() ? ingredient.partialNbt() : null);
            return array;
        }

        throw new JsonSyntaxException("Cannot write HBM ingredient as legacy AStack without concrete item, legacy id, or legacy ore provenance: "
                + ingredient.diagnosticName());
    }

    public static JsonArray writeLegacyItemOutput(HbmItemOutput output) {
        JsonArray array = new JsonArray();
        if (output.oneOf()) {
            array.add("multi");
            for (HbmItemOutput.Entry entry : output.entries()) {
                JsonArray child = new JsonArray();
                child.add(writeLegacyItemStack(entry.stack()));
                if (entry.chance() < 1.0F || entry.weight() > 0) {
                    child.add(entry.chance());
                }
                if (entry.weight() > 0) {
                    child.add(entry.weight());
                }
                array.add(child);
            }
            return array;
        }

        HbmItemOutput.Entry entry = output.entries().get(0);
        array.add("single");
        array.add(writeLegacyItemStack(entry.stack()));
        if (entry.chance() < 1.0F) {
            array.add(entry.chance());
        }
        return array;
    }

    public static JsonArray writeLegacyFluidStack(HbmFluidStack stack) {
        JsonArray array = new JsonArray();
        array.add(stack.type().getName());
        array.add(stack.amount());
        if (stack.pressure() != 0) {
            array.add(stack.pressure());
        }
        return array;
    }

    public static JsonObject writeLegacyRecipe(GenericMachineRecipe recipe) {
        JsonObject object = new JsonObject();
        object.addProperty("name", recipe.getInternalName());

        if (!recipe.getItemInputs().isEmpty()) {
            JsonArray inputItems = new JsonArray();
            recipe.getItemInputs().forEach(input -> inputItems.add(writeLegacyAStack(input)));
            object.add("inputItem", inputItems);
        }
        if (!recipe.getFluidInputs().isEmpty()) {
            JsonArray inputFluids = new JsonArray();
            recipe.getFluidInputs().forEach(fluid -> inputFluids.add(writeLegacyFluidStack(fluid)));
            object.add("inputFluid", inputFluids);
        }
        if (!recipe.getItemOutputEntries().isEmpty()) {
            JsonArray outputItems = new JsonArray();
            recipe.getItemOutputEntries().forEach(output -> outputItems.add(writeLegacyItemOutput(output)));
            object.add("outputItem", outputItems);
        }
        if (!recipe.getFluidOutputs().isEmpty()) {
            JsonArray outputFluids = new JsonArray();
            recipe.getFluidOutputs().forEach(fluid -> outputFluids.add(writeLegacyFluidStack(fluid)));
            object.add("outputFluid", outputFluids);
        }
        object.addProperty("duration", recipe.getDuration());
        object.addProperty("power", recipe.getPower());
        if (recipe.hasCustomLocalization()) {
            object.addProperty("named", true);
        }
        if (recipe.getNameWrapper() != null) {
            object.addProperty("nameWrapper", recipe.getNameWrapper());
        }
        if (!recipe.getPools().isEmpty()) {
            object.addProperty("blueprintpool", String.join(":", recipe.getPools()));
        }
        if (recipe.getAutoSwitchGroup() != null) {
            object.addProperty("autoSwitchGroup", recipe.getAutoSwitchGroup());
        }
        recipe.getExtraData().writeToJson(object);
        return object;
    }

    private static List<HbmIngredient> readLegacyAStackArray(JsonArray array) {
        List<HbmIngredient> ingredients = new ArrayList<>();
        for (JsonElement element : array) {
            ingredients.add(readLegacyAStack(element.getAsJsonArray()));
        }
        return List.copyOf(ingredients);
    }

    private static HbmIngredient readLegacyAStack(JsonArray array) {
        String type = array.get(0).getAsString();
        int count = array.size() > 2 ? array.get(2).getAsInt() : 1;
        return switch (type) {
            case "item" -> readLegacyItemInput(array, count, false);
            case "nbt" -> readLegacyItemInput(array, count, true);
            case "dict" -> HbmIngredient.legacyOre(array.get(1).getAsString(), count);
            default -> throw new JsonSyntaxException("Unsupported legacy AStack type: " + type);
        };
    }

    private static HbmIngredient readLegacyItemInput(JsonArray array, int count, boolean nbt) {
        ResourceLocation legacyId = normalizeLegacyId(array.get(1).getAsString());
        int legacyMeta = array.size() > 3 ? array.get(3).getAsInt() : 0;
        if (nbt) {
            ItemStack stack = readLegacyItemStack(legacyId, count, 0);
            if (array.size() > 4) {
                stack.setTag(parseNbt(array.get(4).getAsString(), "legacy NBTStack"));
                return HbmIngredient.partialNbt(stack);
            }
            return HbmIngredient.exact(stack);
        }
        if (!nbt) {
            if (legacyMeta == HbmIngredient.WILDCARD_META) {
                return HbmIngredient.legacyWildcard(legacyId, count);
            }
            if (LegacyMetaItemMappings.hasMapping(legacyId, legacyMeta)) {
                return HbmIngredient.legacyMeta(legacyId, legacyMeta, count);
            }
        }

        ItemStack stack = readLegacyItemStack(legacyId, count, legacyMeta);
        return HbmIngredient.legacyItem(legacyId, legacyMeta, stack);
    }

    private static List<HbmItemOutput> readLegacyOutputArray(JsonArray array) {
        List<HbmItemOutput> outputs = new ArrayList<>();
        for (JsonElement element : array) {
            outputs.add(readLegacyOutput(element.getAsJsonArray()));
        }
        return List.copyOf(outputs);
    }

    private static HbmItemOutput readLegacyOutput(JsonArray array) {
        String type = array.get(0).getAsString();
        if ("single".equals(type)) {
            ItemStack stack = readLegacyItemStack(array.get(1).getAsJsonArray());
            float chance = array.size() > 2 ? array.get(2).getAsFloat() : 1.0F;
            return HbmItemOutput.chance(stack, chance);
        }
        if ("multi".equals(type)) {
            List<HbmItemOutput.Entry> entries = new ArrayList<>();
            for (JsonElement element : array) {
                if (!element.isJsonArray()) {
                    continue;
                }
                entries.add(readLegacyChanceOutputEntry(element.getAsJsonArray()));
            }
            return HbmItemOutput.oneOf(entries);
        }
        throw new JsonSyntaxException("Unsupported legacy item output type: " + type);
    }

    private static HbmItemOutput.Entry readLegacyChanceOutputEntry(JsonArray array) {
        if (array.get(0).isJsonPrimitive()) {
            ItemStack stack = readLegacyItemStack(array.get(1).getAsJsonArray());
            float chance = array.size() > 2 ? array.get(2).getAsFloat() : 1.0F;
            int weight = array.size() > 3 ? array.get(3).getAsInt() : 0;
            return new HbmItemOutput.Entry(stack, chance, weight);
        }

        ItemStack stack = readLegacyItemStack(array.get(0).getAsJsonArray());
        float chance = array.size() > 1 ? array.get(1).getAsFloat() : 1.0F;
        int weight = array.size() > 2 ? array.get(2).getAsInt() : 0;
        return new HbmItemOutput.Entry(stack, chance, weight);
    }

    private static ItemStack readLegacyItemStack(JsonArray array) {
        ResourceLocation legacyId = normalizeLegacyId(array.get(0).getAsString());
        int count = array.size() > 1 ? array.get(1).getAsInt() : 1;
        int legacyMeta = array.size() > 2 ? array.get(2).getAsInt() : 0;
        ItemStack stack = readLegacyItemStack(legacyId, count, legacyMeta);
        if (array.size() > 3) {
            stack.setTag(parseNbt(array.get(3).getAsString(), "legacy ItemStack"));
        }
        return stack;
    }

    public static ItemStack readLegacyRecipeItemStack(JsonArray array) {
        return readLegacyItemStack(array);
    }

    public static HbmIngredient readLegacyRecipeAStack(JsonArray array) {
        return readLegacyAStack(array);
    }

    private static ItemStack readLegacyItemStack(ResourceLocation legacyId, int count, int legacyMeta) {
        Optional<ItemStack> mappedStack = LegacyMetaItemMappings.stackPreservingCount(legacyId, legacyMeta, count);
        if (mappedStack.isPresent()) {
            return mappedStack.get();
        }
        if (legacyMeta != 0) {
            throw new JsonSyntaxException("Missing legacy item meta mapping: " + legacyId + " meta " + legacyMeta);
        }

        Item item = HbmRegistryUtil.item(modernIdForRegistryLookup(legacyId))
                .or(() -> {
                    if (!isHbmLegacyNamespace(legacyId)) {
                        return java.util.Optional.empty();
                    }
                    RegistryObject<Item> legacyItem = ModItems.legacyItem(legacyId.getPath());
                    return legacyItem == null ? java.util.Optional.empty() : java.util.Optional.of(legacyItem.get());
                })
                .orElseThrow(() -> new JsonSyntaxException("Unknown legacy item: " + legacyId));
        return new ItemStack(item, count);
    }

    private static List<HbmFluidStack> readModernFluidStacks(JsonArray array) {
        return array.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(object -> HbmFluidJsonUtil.readFluidStack(object, "generic machine fluid stack"))
                .toList();
    }

    private static List<HbmFluidStack> readLegacyFluidArray(JsonArray array) {
        List<HbmFluidStack> fluids = new ArrayList<>();
        for (JsonElement element : array) {
            JsonArray legacy = element.getAsJsonArray();
            JsonObject modern = new JsonObject();
            modern.add("fluid", legacy.get(0));
            modern.add("amount", legacy.get(1));
            if (legacy.size() > 2) {
                modern.add("pressure", legacy.get(2));
            }
            fluids.add(HbmFluidJsonUtil.readFluidStack(modern, "legacy generic machine fluid stack"));
        }
        return List.copyOf(fluids);
    }

    private static Optional<ItemStack> representativeInputStack(HbmIngredient ingredient) {
        if (ingredient.hasExactStack()) {
            return Optional.of(ingredient.exactStack());
        }
        if (!ingredient.isSingleItemIngredient()) {
            return Optional.empty();
        }
        List<ItemStack> displayStacks = ingredient.displayStacks();
        if (displayStacks.size() == 1) {
            return Optional.of(displayStacks.get(0));
        }
        return Optional.empty();
    }

    public static JsonArray writeLegacyItemStack(ItemStack stack) {
        JsonArray array = new JsonArray();
        writeLegacyItemStackBody(array, stack, stack.getCount(), stack.getTag());
        return array;
    }

    private static void writeLegacyItemStackBody(JsonArray array, ItemStack stack, int count, CompoundTag tag) {
        Optional<LegacyMetaItemMappings.LegacyStackIdentity> legacyIdentity =
                LegacyMetaItemMappings.legacyIdentity(stack);
        ResourceLocation itemId = legacyIdentity
                .map(LegacyMetaItemMappings.LegacyStackIdentity::legacyId)
                .orElseGet(() -> legacyItemId(HbmRegistryUtil.itemKey(stack.getItem())));
        int meta = legacyIdentity
                .map(LegacyMetaItemMappings.LegacyStackIdentity::legacyMeta)
                .orElseGet(stack::getDamageValue);
        CompoundTag legacyTag = stripSyntheticDamageTag(tag, meta);
        boolean hasTag = legacyTag != null && !legacyTag.isEmpty();
        array.add(itemId.toString());
        if (count != 1 || meta != 0 || hasTag) {
            array.add(count);
        }
        if (meta != 0 || hasTag) {
            array.add(meta);
        }
        if (hasTag) {
            array.add(legacyTag.toString());
        }
    }

    private static CompoundTag stripSyntheticDamageTag(CompoundTag tag, int meta) {
        if (tag == null || tag.isEmpty() || !tag.contains("Damage")) {
            return tag;
        }
        if (tag.getInt("Damage") != meta) {
            return tag;
        }
        CompoundTag copy = tag.copy();
        copy.remove("Damage");
        return copy;
    }

    private static ResourceLocation normalizeLegacyId(String id) {
        ResourceLocation parsed = id.contains(":") ? new ResourceLocation(id) : new ResourceLocation("hbm", id);
        return isHbmLegacyNamespace(parsed) ? new ResourceLocation("hbm", parsed.getPath()) : parsed;
    }

    private static ResourceLocation modernIdForRegistryLookup(ResourceLocation legacyId) {
        return isHbmLegacyNamespace(legacyId) ? new ResourceLocation(HbmNtm.MOD_ID, legacyId.getPath()) : legacyId;
    }

    private static ResourceLocation legacyItemId(ResourceLocation itemId) {
        return HbmNtm.MOD_ID.equals(itemId.getNamespace()) ? new ResourceLocation("hbm", itemId.getPath()) : itemId;
    }

    private static boolean isHbmLegacyNamespace(ResourceLocation id) {
        return "hbm".equals(id.getNamespace()) || HbmNtm.MOD_ID.equals(id.getNamespace());
    }

    private static CompoundTag parseNbt(String nbt, String name) {
        try {
            return TagParser.parseTag(nbt);
        } catch (CommandSyntaxException exception) {
            throw new JsonSyntaxException("Invalid NBT in " + name + ": " + exception.getMessage(), exception);
        }
    }

    private static String getString(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }
}
