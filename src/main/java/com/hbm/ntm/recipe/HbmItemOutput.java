package com.hbm.ntm.recipe;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HbmItemOutput {
    private static final String TYPE_ONE_OF = "one_of";

    private final List<Entry> entries;
    private final boolean oneOf;

    private HbmItemOutput(List<Entry> entries, boolean oneOf) {
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("HBM item output must have at least one entry");
        }
        this.entries = List.copyOf(entries);
        this.oneOf = oneOf;
    }

    public static HbmItemOutput of(ItemStack stack) {
        return chance(stack, 1.0F);
    }

    public static HbmItemOutput chance(ItemStack stack, float chance) {
        return new HbmItemOutput(List.of(new Entry(stack, chance, 0)), false);
    }

    public static HbmItemOutput oneOf(List<Entry> entries) {
        return new HbmItemOutput(entries, true);
    }

    public List<Entry> entries() {
        return entries;
    }

    public boolean oneOf() {
        return oneOf;
    }

    public ItemStack representativeStack() {
        return entries.get(0).stack();
    }

    public List<ItemStack> displayStacks() {
        return displayOptions().stream()
                .map(DisplayOption::stack)
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    public List<DisplayOption> displayOptions() {
        if (!oneOf) {
            Entry entry = entries.get(0);
            return List.of(new DisplayOption(entry.stack(), entry.chance(), labelFor(entry.stack(), entry.chance())));
        }

        int totalWeight = totalPositiveWeight();
        List<DisplayOption> options = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            float effectiveChance = totalWeight <= 0 ? 0.0F : (float) entry.weight() / (float) totalWeight * entry.chance();
            options.add(new DisplayOption(entry.stack(), effectiveChance, labelFor(entry.stack(), effectiveChance)));
        }
        return List.copyOf(options);
    }

    public List<String> displayLabels() {
        if (!oneOf) {
            return List.of(labelFor(entries.get(0).stack(), entries.get(0).chance()));
        }

        List<String> labels = new ArrayList<>(entries.size() + 1);
        labels.add("One of:");
        displayOptions().forEach(option -> labels.add("  " + option.label()));
        return List.copyOf(labels);
    }

    public ItemStack collapse(RandomSource random) {
        Entry entry = oneOf ? chooseWeighted(random) : entries.get(0);
        return entry.roll(random);
    }

    public JsonObject toJson() {
        if (!oneOf && entries.size() == 1) {
            return entries.get(0).toJson(false);
        }

        JsonObject object = new JsonObject();
        object.addProperty("type", TYPE_ONE_OF);
        JsonArray array = new JsonArray();
        entries.forEach(entry -> array.add(entry.toJson(true)));
        object.add("entries", array);
        return object;
    }

    public static HbmItemOutput fromJson(JsonObject object) {
        String type = GsonHelper.getAsString(object, "type", "single");
        if (TYPE_ONE_OF.equals(type)) {
            JsonArray array = GsonHelper.getAsJsonArray(object, "entries");
            List<Entry> entries = array.asList().stream()
                    .map(element -> Entry.fromJson(GsonHelper.convertToJsonObject(element, "item output entry")))
                    .toList();
            return oneOf(entries);
        }
        return chance(readItemStack(object, "item output"), GsonHelper.getAsFloat(object, "chance", 1.0F));
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(oneOf);
        buffer.writeCollection(entries, (output, entry) -> entry.toNetwork(output));
    }

    public static HbmItemOutput fromNetwork(FriendlyByteBuf buffer) {
        boolean oneOf = buffer.readBoolean();
        List<Entry> entries = buffer.readList(Entry::fromNetwork);
        return new HbmItemOutput(entries, oneOf);
    }

    private Entry chooseWeighted(RandomSource random) {
        int totalWeight = totalPositiveWeight();
        if (totalWeight <= 0) {
            return entries.get(random.nextInt(entries.size()));
        }

        int target = random.nextInt(totalWeight);
        for (Entry entry : entries) {
            target -= Math.max(0, entry.weight());
            if (target < 0) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }

    public int totalPositiveWeight() {
        return entries.stream()
                .mapToInt(entry -> Math.max(0, entry.weight()))
                .sum();
    }

    public boolean hasValidWeightedChoices() {
        return !oneOf || totalPositiveWeight() > 0;
    }

    private static String labelFor(ItemStack stack, float chance) {
        String label = stack.getCount() + "x " + stack.getHoverName().getString();
        if (chance < 1.0F) {
            label += " (" + percent(chance) + "%)";
        }
        return label;
    }

    private static String percent(float chance) {
        return String.format(Locale.US, "%.1f", Math.floor(chance * 1000.0F) / 10.0F);
    }

    public record DisplayOption(ItemStack stack, float chance, String label) {
        public DisplayOption {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            chance = Math.max(0.0F, Math.min(1.0F, chance));
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    public record Entry(ItemStack stack, float chance, int weight) {
        public Entry {
            if (stack == null || stack.isEmpty()) {
                throw new IllegalArgumentException("HBM item output entry stack cannot be empty");
            }
            stack = stack.copy();
            chance = Math.max(0.0F, Math.min(1.0F, chance));
            weight = Math.max(0, weight);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }

        private ItemStack roll(RandomSource random) {
            if (chance >= 1.0F) {
                return stack();
            }

            int count = 0;
            for (int i = 0; i < stack.getCount(); i++) {
                if (random.nextFloat() <= chance) {
                    count++;
                }
            }
            if (count <= 0) {
                return ItemStack.EMPTY;
            }

            ItemStack rolled = stack();
            rolled.setCount(count);
            return rolled;
        }

        private JsonObject toJson(boolean includeWeight) {
            JsonObject object = itemStackJson(stack);
            if (chance < 1.0F) {
                object.addProperty("chance", chance);
            }
            if (includeWeight && weight > 0) {
                object.addProperty("weight", weight);
            }
            return object;
        }

        private static Entry fromJson(JsonObject object) {
            ItemStack stack = readItemStack(object, "item output entry");
            float chance = GsonHelper.getAsFloat(object, "chance", 1.0F);
            int weight = GsonHelper.getAsInt(object, "weight", 0);
            return new Entry(stack, chance, weight);
        }

        private void toNetwork(FriendlyByteBuf buffer) {
            buffer.writeItem(stack);
            buffer.writeFloat(chance);
            buffer.writeVarInt(weight);
        }

        private static Entry fromNetwork(FriendlyByteBuf buffer) {
            return new Entry(buffer.readItem(), buffer.readFloat(), buffer.readVarInt());
        }
    }

    private static ItemStack readItemStack(JsonObject object, String name) {
        if (!object.has("item") && object.has("tag")) {
            return readTagStack(object, name);
        }

        String itemName = GsonHelper.getAsString(object, "item");
        ResourceLocation itemId = new ResourceLocation(itemName);
        Item item = HbmRegistryUtil.item(itemId)
                .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + itemName + "' in " + name));
        return stackFromItem(object, name, item);
    }

    private static ItemStack readTagStack(JsonObject object, String name) {
        String tagName = GsonHelper.getAsString(object, "tag");
        ResourceLocation tagId = new ResourceLocation(tagName);
        TagKey<Item> tagKey = ItemTags.create(tagId);
        ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
        if (tags == null) {
            throw new JsonSyntaxException("Item tags are not available while resolving output tag '#" + tagName
                    + "' in " + name);
        }
        ITag<Item> tag = tags.getTag(tagKey);
        Item item = tag.stream()
                .findFirst()
                .orElseThrow(() -> new JsonSyntaxException("Empty item tag '#" + tagName + "' in " + name));
        return stackFromItem(object, name, item);
    }

    private static ItemStack stackFromItem(JsonObject object, String name, Item item) {
        int count = GsonHelper.getAsInt(object, "count", 1);
        if (count < 1) {
            throw new JsonSyntaxException("Invalid item count " + count + " in " + name);
        }

        ItemStack stack = new ItemStack(item, count);
        if (object.has("nbt")) {
            try {
                CompoundTag tag = TagParser.parseTag(GsonHelper.getAsString(object, "nbt"));
                stack.setTag(tag);
            } catch (CommandSyntaxException exception) {
                throw new JsonSyntaxException("Invalid item NBT in " + name + ": " + exception.getMessage(), exception);
            }
        }
        return stack;
    }

    private static JsonObject itemStackJson(ItemStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("item", HbmRegistryUtil.itemKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            object.addProperty("count", stack.getCount());
        }
        if (stack.hasTag() && !stack.getTag().isEmpty()) {
            object.addProperty("nbt", stack.getTag().toString());
        }
        return object;
    }
}
