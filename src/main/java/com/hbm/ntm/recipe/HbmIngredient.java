package com.hbm.ntm.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record HbmIngredient(Ingredient ingredient, int count, ItemStack exactStack,
        CompoundTag partialNbt, @Nullable ResourceLocation legacyId, int legacyMeta, boolean legacyWildcard,
        @Nullable String legacyOreName) {
    public static final int WILDCARD_META = Short.MAX_VALUE;

    public HbmIngredient {
        count = Math.max(1, count);
        exactStack = exactStack == null ? ItemStack.EMPTY : exactStack.copy();
        partialNbt = partialNbt == null ? new CompoundTag() : partialNbt.copy();
        legacyOreName = legacyOreName == null || legacyOreName.isBlank() ? null : legacyOreName;
    }

    public static HbmIngredient of(ItemLike item, int count) {
        return new HbmIngredient(Ingredient.of(item), count, ItemStack.EMPTY, new CompoundTag(), null, -1, false, null);
    }

    public static HbmIngredient of(TagKey<Item> tag, int count) {
        return new HbmIngredient(Ingredient.of(tag), count, ItemStack.EMPTY, new CompoundTag(), null, -1, false, null);
    }

    public static HbmIngredient legacyOre(String legacyOreName, int count) {
        return new HbmIngredient(Ingredient.of(LegacyOreDictionaryMappings.itemTag(legacyOreName)), count,
                ItemStack.EMPTY, new CompoundTag(), null, -1, false, legacyOreName);
    }

    public static HbmIngredient exact(ItemStack stack) {
        return new HbmIngredient(Ingredient.of(stack), stack.getCount(), stack, new CompoundTag(), null, -1, false, null);
    }

    public static HbmIngredient partialNbt(ItemStack stack) {
        CompoundTag tag = stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        ItemStack displayStack = stack.copy();
        displayStack.setTag(null);
        return new HbmIngredient(Ingredient.of(displayStack), stack.getCount(), ItemStack.EMPTY, tag, null, -1, false, null);
    }

    public static HbmIngredient legacyMeta(ResourceLocation legacyId, int legacyMeta, int count) {
        RegistryObject<Item> item = LegacyMetaItemMappings.requireItem(legacyId, legacyMeta);
        return new HbmIngredient(Ingredient.of(item.get()), count, ItemStack.EMPTY, new CompoundTag(), legacyId, legacyMeta, false, null);
    }

    public static HbmIngredient legacyWildcard(ResourceLocation legacyId, int count) {
        List<RegistryObject<Item>> variants = LegacyMetaItemMappings.variants(legacyId);
        if (variants.isEmpty()) {
            throw new IllegalStateException("Missing legacy wildcard item mapping: " + legacyId);
        }
        ItemStack[] stacks = variants.stream()
                .map(item -> new ItemStack(item.get()))
                .toArray(ItemStack[]::new);
        return new HbmIngredient(Ingredient.of(stacks), count, ItemStack.EMPTY, new CompoundTag(), legacyId, WILDCARD_META, true, null);
    }

    public boolean test(ItemStack stack) {
        return test(stack, false);
    }

    public boolean test(ItemStack stack, boolean ignoreSize) {
        if (!ignoreSize && stack.getCount() < count) {
            return false;
        }
        if (!ingredient.test(stack)) {
            return false;
        }
        if (!exactStack.isEmpty() && !ItemStack.isSameItemSameTags(exactStack, stack)) {
            return false;
        }
        return partialNbt.isEmpty() || matchesPartialNbt(stack);
    }

    public boolean hasExactStack() {
        return !exactStack.isEmpty();
    }

    @Override
    public ItemStack exactStack() {
        return exactStack.copy();
    }

    @Override
    public CompoundTag partialNbt() {
        return partialNbt.copy();
    }

    public boolean hasPartialNbt() {
        return !partialNbt.isEmpty();
    }

    public Optional<ItemStack> mappedLegacyStack() {
        if (legacyId == null) {
            return Optional.empty();
        }
        if (legacyWildcard) {
            return LegacyMetaItemMappings.stack(legacyId, 0, count);
        }
        return LegacyMetaItemMappings.stack(legacyId, legacyMeta, count);
    }

    public List<ItemStack> displayStacks() {
        if (legacyId != null) {
            if (legacyWildcard) {
                return LegacyMetaItemMappings.stacks(legacyId, count);
            }
            return mappedLegacyStack().map(List::of).orElseGet(List::of);
        }
        if (hasExactStack()) {
            return List.of(exactStack());
        }
        return Arrays.stream(ingredient.getItems())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(count);
                    if (hasPartialNbt()) {
                        copy.setTag(partialNbt());
                    }
                    return copy;
                })
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    public boolean hasDisplayStacks() {
        return !displayStacks().isEmpty();
    }

    public boolean unresolvedDisplayInput() {
        return displayStacks().isEmpty();
    }

    public String diagnosticName() {
        if (legacyOreName != null) {
            return "legacy ore " + legacyOreName + " -> #" + LegacyOreDictionaryMappings.itemTagId(legacyOreName);
        }
        if (legacyId != null) {
            return legacyWildcard
                    ? "legacy wildcard " + legacyId
                    : "legacy item " + legacyId + " meta " + legacyMeta;
        }
        if (hasExactStack()) {
            return BuiltInRegistries.ITEM.getKey(exactStack.getItem()).toString();
        }
        String tagId = ingredientTagId();
        if (tagId != null) {
            return "tag #" + tagId;
        }
        String itemId = ingredientItemId();
        if (itemId != null) {
            return "item " + itemId;
        }
        return ingredient.toJson().toString();
    }

    public String diagnosticKey() {
        if (legacyOreName != null) {
            return "legacy_ore:" + legacyOreName + "->" + LegacyOreDictionaryMappings.itemTagId(legacyOreName);
        }
        if (legacyId != null) {
            return legacyWildcard
                    ? "legacy_meta:" + legacyId + ":*"
                    : "legacy_meta:" + legacyId + ":" + legacyMeta;
        }
        if (hasExactStack()) {
            return "item:" + BuiltInRegistries.ITEM.getKey(exactStack.getItem());
        }
        String tagId = ingredientTagId();
        if (tagId != null) {
            return "tag:" + tagId;
        }
        String itemId = ingredientItemId();
        if (itemId != null) {
            return "item:" + itemId;
        }
        return "ingredient:" + ingredient.toJson();
    }

    public boolean exceedsStackLimit() {
        return stackLimit().filter(limit -> count > limit).isPresent();
    }

    public Optional<Integer> stackLimit() {
        List<ItemStack> stacks = displayStacks();
        if (stacks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(stacks.stream()
                .mapToInt(stack -> stack.getItem().getMaxStackSize(stack))
                .max()
                .orElse(64));
    }

    public JsonObject toJson() {
        JsonObject entry = new JsonObject();
        entry.add("ingredient", ingredient.toJson());
        entry.addProperty("count", count);
        if (hasExactStack() && exactStack.hasTag() && !exactStack.getTag().isEmpty()) {
            entry.add("exact_stack", itemStackJson(exactStack));
        }
        if (hasPartialNbt()) {
            entry.addProperty("partial_nbt", partialNbt.toString());
        }
        if (legacyId != null) {
            entry.addProperty("legacy_id", legacyId.toString());
            entry.addProperty("legacy_meta", legacyMeta);
            if (legacyWildcard) {
                entry.addProperty("legacy_wildcard", true);
            }
        }
        if (legacyOreName != null) {
            entry.addProperty("legacy_ore", legacyOreName);
        }
        return entry;
    }

    public static HbmIngredient fromJson(JsonObject object) {
        Ingredient ingredient = Ingredient.fromJson(object.get("ingredient"));
        int count = object.has("count") ? object.get("count").getAsInt() : 1;
        ItemStack exactStack = object.has("exact_stack")
                ? readItemStack(object.getAsJsonObject("exact_stack"), "exact stack")
                : ItemStack.EMPTY;
        CompoundTag partialNbt = object.has("partial_nbt")
                ? readNbtString(object.get("partial_nbt").getAsString(), "partial_nbt")
                : new CompoundTag();
        if (!exactStack.isEmpty() && !partialNbt.isEmpty()) {
            throw new JsonSyntaxException("HBM ingredient cannot define both exact_stack and partial_nbt");
        }
        ResourceLocation legacyId = object.has("legacy_id") ? new ResourceLocation(object.get("legacy_id").getAsString()) : null;
        int legacyMeta = object.has("legacy_meta") ? object.get("legacy_meta").getAsInt() : -1;
        boolean legacyWildcard = object.has("legacy_wildcard") && object.get("legacy_wildcard").getAsBoolean();
        if (legacyId != null && legacyWildcard && LegacyMetaItemMappings.variants(legacyId).isEmpty()) {
            throw new JsonSyntaxException("Missing legacy wildcard item mapping: " + legacyId);
        }
        if (legacyId != null && !legacyWildcard && LegacyMetaItemMappings.item(legacyId, legacyMeta).isEmpty()) {
            throw new JsonSyntaxException("Missing legacy item mapping: " + legacyId + " meta " + legacyMeta);
        }
        String legacyOreName = object.has("legacy_ore") ? object.get("legacy_ore").getAsString() : null;
        return new HbmIngredient(ingredient, count, exactStack, partialNbt, legacyId, legacyMeta, legacyWildcard,
                legacyOreName);
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        ingredient.toNetwork(buffer);
        buffer.writeVarInt(count);
        buffer.writeBoolean(hasExactStack());
        if (hasExactStack()) {
            buffer.writeItem(exactStack);
        }
        buffer.writeBoolean(hasPartialNbt());
        if (hasPartialNbt()) {
            buffer.writeNbt(partialNbt);
        }
        buffer.writeBoolean(legacyId != null);
        if (legacyId != null) {
            buffer.writeResourceLocation(legacyId);
            buffer.writeVarInt(legacyMeta);
            buffer.writeBoolean(legacyWildcard);
        }
        buffer.writeBoolean(legacyOreName != null);
        if (legacyOreName != null) {
            buffer.writeUtf(legacyOreName);
        }
    }

    public static HbmIngredient fromNetwork(FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        int count = buffer.readVarInt();
        ItemStack exactStack = buffer.readBoolean() ? buffer.readItem() : ItemStack.EMPTY;
        CompoundTag partialNbt = buffer.readBoolean() ? buffer.readNbt() : new CompoundTag();
        ResourceLocation legacyId = null;
        int legacyMeta = -1;
        boolean legacyWildcard = false;
        if (buffer.readBoolean()) {
            legacyId = buffer.readResourceLocation();
            legacyMeta = buffer.readVarInt();
            legacyWildcard = buffer.readBoolean();
        }
        String legacyOreName = buffer.readBoolean() ? buffer.readUtf() : null;
        return new HbmIngredient(ingredient, count, exactStack, partialNbt, legacyId, legacyMeta, legacyWildcard,
                legacyOreName);
    }

    private boolean matchesPartialNbt(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        CompoundTag stackTag = stack.getTag();
        for (String key : partialNbt.getAllKeys()) {
            Tag wanted = partialNbt.get(key);
            Tag actual = stackTag.get(key);
            if (actual == null || !wanted.equals(actual)) {
                return false;
            }
        }
        return true;
    }

    private static ItemStack readItemStack(JsonObject object, String name) {
        String itemName = object.get("item").getAsString();
        ResourceLocation itemId = new ResourceLocation(itemName);
        Item item = BuiltInRegistries.ITEM.getOptional(itemId)
                .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + itemName + "' in " + name));
        int count = object.has("count") ? object.get("count").getAsInt() : 1;
        if (count < 1) {
            throw new JsonSyntaxException("Invalid item count " + count + " in " + name);
        }
        ItemStack stack = new ItemStack(item, count);
        if (object.has("nbt")) {
            try {
                CompoundTag tag = TagParser.parseTag(object.get("nbt").getAsString());
                stack.setTag(tag);
            } catch (CommandSyntaxException exception) {
                throw new JsonSyntaxException("Invalid item NBT in " + name + ": " + exception.getMessage(), exception);
            }
        }
        return stack;
    }

    private static CompoundTag readNbtString(String nbt, String name) {
        try {
            return TagParser.parseTag(nbt);
        } catch (CommandSyntaxException exception) {
            throw new JsonSyntaxException("Invalid item NBT in " + name + ": " + exception.getMessage(), exception);
        }
    }

    private static JsonObject itemStackJson(ItemStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            object.addProperty("count", stack.getCount());
        }
        if (stack.hasTag() && !stack.getTag().isEmpty()) {
            object.addProperty("nbt", stack.getTag().toString());
        }
        return object;
    }

    @Nullable
    private String ingredientTagId() {
        JsonObject object = singleIngredientObject();
        return object != null && object.has("tag") ? object.get("tag").getAsString() : null;
    }

    @Nullable
    private String ingredientItemId() {
        JsonObject object = singleIngredientObject();
        return object != null && object.has("item") ? object.get("item").getAsString() : null;
    }

    @Nullable
    private JsonObject singleIngredientObject() {
        JsonElement json = ingredient.toJson();
        return json.isJsonObject() ? json.getAsJsonObject() : null;
    }
}
