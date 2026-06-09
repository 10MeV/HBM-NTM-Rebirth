package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RTTYPatternMatcher {
    public static final String MODE_EXACT = "exact";
    public static final String MODE_WILDCARD = "wildcard";

    private static final String TAG_MODE_PREFIX = "mode";

    private final String[] modes;

    public RTTYPatternMatcher(int count) {
        this.modes = new String[Math.max(0, count)];
    }

    public String mode(int index) {
        return index >= 0 && index < modes.length ? modes[index] : null;
    }

    public void initPatternStandard(ItemStack stack, int index) {
        if (!validIndex(index)) {
            return;
        }
        if (stack.isEmpty()) {
            modes[index] = null;
        } else if (stack.isDamageableItem()) {
            modes[index] = MODE_EXACT;
        } else {
            modes[index] = MODE_WILDCARD;
        }
    }

    public void nextMode(ItemStack pattern, int index) {
        if (!validIndex(index)) {
            return;
        }
        if (pattern.isEmpty()) {
            modes[index] = null;
            return;
        }
        if (modes[index] == null) {
            modes[index] = MODE_EXACT;
            return;
        }
        if (MODE_EXACT.equals(modes[index])) {
            modes[index] = MODE_WILDCARD;
            return;
        }
        List<String> tags = tagNames(pattern);
        if (MODE_WILDCARD.equals(modes[index])) {
            modes[index] = tags.isEmpty() ? MODE_EXACT : tags.get(0);
            return;
        }
        int current = tags.indexOf(modes[index]);
        modes[index] = current >= 0 && current + 1 < tags.size() ? tags.get(current + 1) : MODE_EXACT;
    }

    public boolean isValidForFilter(ItemStack filter, int index, ItemStack input) {
        if (filter.isEmpty() || input.isEmpty() || !validIndex(index)) {
            return false;
        }
        String mode = modes[index];
        if (mode == null) {
            modes[index] = mode = MODE_EXACT;
        }
        if (MODE_EXACT.equals(mode)) {
            return input.getItem() == filter.getItem() && input.getDamageValue() == filter.getDamageValue();
        }
        if (MODE_WILDCARD.equals(mode)) {
            return input.getItem() == filter.getItem();
        }
        ResourceLocation tagId = ResourceLocation.tryParse(mode);
        return tagId != null && input.is(TagKey.create(Registries.ITEM, tagId));
    }

    public void save(CompoundTag tag) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] != null) {
                tag.putString(TAG_MODE_PREFIX + i, modes[i]);
            }
        }
    }

    public void load(CompoundTag tag) {
        for (int i = 0; i < modes.length; i++) {
            modes[i] = tag.contains(TAG_MODE_PREFIX + i, Tag.TAG_STRING)
                    ? tag.getString(TAG_MODE_PREFIX + i)
                    : null;
        }
    }

    public String label(int index) {
        return getLabel(mode(index));
    }

    public static String getLabel(String mode) {
        if (MODE_EXACT.equals(mode)) {
            return "Item and damage match";
        }
        if (MODE_WILDCARD.equals(mode)) {
            return "Item matches";
        }
        if (mode == null || mode.isEmpty()) {
            return "No filter";
        }
        return "Item tag matches: " + mode;
    }

    private boolean validIndex(int index) {
        return index >= 0 && index < modes.length;
    }

    private static List<String> tagNames(ItemStack stack) {
        List<String> names = new ArrayList<>();
        if (!stack.isEmpty()) {
            stack.getItem().builtInRegistryHolder()
                    .tags()
                    .map(TagKey<Item>::location)
                    .map(ResourceLocation::toString)
                    .sorted()
                    .forEach(names::add);
        }
        return names;
    }
}
