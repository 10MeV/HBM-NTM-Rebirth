package com.hbm.ntm.util;

import com.hbm.ntm.item.BedrockOreItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class LegacyPatternMatcher {
    public static final String MODE_EXACT = "exact";
    public static final String MODE_WILDCARD = "wildcard";
    public static final String MODE_BEDROCK = "bedrock";

    private final String[] modes;

    public LegacyPatternMatcher(int count) {
        this.modes = new String[count];
    }

    public void initPatternSmart(ItemStack stack, int slot) {
        if (slot < 0 || slot >= modes.length) {
            return;
        }
        if (stack.isEmpty()) {
            modes[slot] = null;
            return;
        }
        List<String> names = HbmItemStackUtil.getOreDictNames(stack);
        if (setFirstMatchingTag(names, slot, "ingot")) return;
        if (setFirstMatchingTag(names, slot, "block")) return;
        if (setFirstMatchingTag(names, slot, "dust")) return;
        if (setFirstMatchingTag(names, slot, "nugget")) return;
        if (setFirstMatchingTag(names, slot, "plate")) return;
        initPatternStandard(stack, slot);
    }

    public void initPatternStandard(ItemStack stack, int slot) {
        if (slot < 0 || slot >= modes.length) {
            return;
        }
        if (stack.isEmpty()) {
            modes[slot] = null;
        } else if (stack.getItem() instanceof BedrockOreItem) {
            modes[slot] = MODE_BEDROCK;
        } else if (stack.isDamageableItem()) {
            modes[slot] = MODE_EXACT;
        } else {
            modes[slot] = MODE_WILDCARD;
        }
    }

    private boolean setFirstMatchingTag(List<String> names, int slot, String prefix) {
        for (String name : names) {
            if (tagMatchesPrefix(name, prefix)) {
                modes[slot] = name;
                return true;
            }
        }
        return false;
    }

    private static boolean tagMatchesPrefix(String name, String prefix) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        String path = id == null ? name : id.getPath();
        return path.startsWith(prefix);
    }

    public void nextMode(ItemStack pattern, int slot) {
        if (slot < 0 || slot >= modes.length) {
            return;
        }
        if (pattern.isEmpty()) {
            modes[slot] = null;
            return;
        }
        String mode = modes[slot];
        if (mode == null) {
            modes[slot] = MODE_EXACT;
        } else if (MODE_EXACT.equals(mode)) {
            modes[slot] = pattern.getItem() instanceof BedrockOreItem ? MODE_BEDROCK : MODE_WILDCARD;
        } else if (MODE_BEDROCK.equals(mode)) {
            modes[slot] = MODE_WILDCARD;
        } else if (MODE_WILDCARD.equals(mode)) {
            List<String> names = HbmItemStackUtil.getOreDictNames(pattern);
            modes[slot] = names.isEmpty() ? MODE_EXACT : names.get(0);
        } else {
            List<String> names = HbmItemStackUtil.getOreDictNames(pattern);
            int index = names.indexOf(mode);
            modes[slot] = index >= 0 && index < names.size() - 1 ? names.get(index + 1) : MODE_EXACT;
        }
    }

    public boolean isValidForFilter(ItemStack filter, int slot, ItemStack input) {
        if (filter.isEmpty() || input.isEmpty()) {
            return false;
        }
        String mode = getMode(slot);
        if (mode == null) {
            mode = MODE_EXACT;
            modes[slot] = mode;
        }
        return switch (mode) {
            case MODE_EXACT -> ItemStack.isSameItem(input, filter)
                    && input.getDamageValue() == filter.getDamageValue();
            case MODE_WILDCARD -> ItemStack.isSameItem(input, filter);
            case MODE_BEDROCK -> ItemStack.isSameItem(input, filter)
                    && input.getItem() instanceof BedrockOreItem
                    && filter.getItem() instanceof BedrockOreItem
                    && BedrockOreItem.getGrade(input) == BedrockOreItem.getGrade(filter);
            default -> HbmItemStackUtil.getOreDictNames(input).contains(mode);
        };
    }

    public String getMode(int slot) {
        return slot >= 0 && slot < modes.length ? modes[slot] : null;
    }

    public int getModeIndex(ItemStack pattern, int slot) {
        String mode = getMode(slot);
        if (mode == null || pattern.isEmpty()) {
            return -1;
        }
        List<String> options = modeOptions(pattern);
        int index = options.indexOf(mode);
        return index < 0 ? 0 : index;
    }

    public static String modeForIndex(ItemStack pattern, int index) {
        List<String> options = modeOptions(pattern);
        if (options.isEmpty()) {
            return null;
        }
        return options.get(Math.floorMod(index, options.size()));
    }

    public static List<String> modeOptions(ItemStack pattern) {
        if (pattern.isEmpty()) {
            return List.of();
        }
        List<String> options = new ArrayList<>();
        options.add(MODE_EXACT);
        if (pattern.getItem() instanceof BedrockOreItem) {
            options.add(MODE_BEDROCK);
        }
        options.add(MODE_WILDCARD);
        options.addAll(HbmItemStackUtil.getOreDictNames(pattern));
        return options;
    }

    public static Component label(String mode) {
        if (mode == null) {
            return Component.empty();
        }
        return switch (mode) {
            case MODE_EXACT -> Component.literal("Item and meta match");
            case MODE_WILDCARD -> Component.literal("Item matches");
            case MODE_BEDROCK -> Component.literal("Item and bedrock grade match");
            default -> Component.literal("Ore dict key matches: " + mode);
        };
    }

    public void readFromNbt(CompoundTag tag) {
        for (int i = 0; i < modes.length; i++) {
            modes[i] = tag.contains("mode" + i) ? tag.getString("mode" + i) : null;
        }
    }

    public void writeToNbt(CompoundTag tag) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] != null) {
                tag.putString("mode" + i, modes[i]);
            }
        }
    }
}
