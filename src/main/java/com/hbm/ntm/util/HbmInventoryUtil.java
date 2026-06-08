package com.hbm.ntm.util;

import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public final class HbmInventoryUtil {
    private HbmInventoryUtil() {
    }

    public static ItemStack tryAddItemToInventory(ItemStack[] inventory, ItemStack stack) {
        return inventory == null ? HbmItemStackUtil.carefulCopy(stack)
                : tryAddItemToInventory(inventory, 0, inventory.length - 1, stack);
    }

    public static ItemStack tryAddItemToInventory(ItemStack[] inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = tryAddItemToExistingStack(inventory, start, end, stack);
        return remainder.isEmpty() || tryAddItemToNewSlot(inventory, start, end, remainder) ? ItemStack.EMPTY : remainder;
    }

    public static ItemStack tryAddItemToExistingStack(ItemStack[] inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.length - 1); slot++) {
            ItemStack current = emptyIfNull(inventory[slot]);
            if (!current.isEmpty() && HbmItemStackUtil.doesStackDataMatch(current, remainder)) {
                int transfer = Math.min(remainder.getCount(), current.getMaxStackSize() - current.getCount());
                if (transfer > 0) {
                    ItemStack merged = current.copy();
                    merged.grow(transfer);
                    inventory[slot] = merged;
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return remainder;
    }

    public static boolean tryAddItemToNewSlot(ItemStack[] inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (inventory == null || remainder.isEmpty()) {
            return true;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.length - 1); slot++) {
            if (emptyIfNull(inventory[slot]).isEmpty()) {
                inventory[slot] = remainder;
                return true;
            }
        }
        return false;
    }

    public static ItemStack tryAddItemToInventory(NonNullList<ItemStack> inventory, ItemStack stack) {
        return tryAddItemToInventory(inventory, 0, inventory.size() - 1, stack);
    }

    public static ItemStack tryAddItemToInventory(NonNullList<ItemStack> inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = tryAddItemToExistingStack(inventory, start, end, stack);
        return remainder.isEmpty() || tryAddItemToNewSlot(inventory, start, end, remainder) ? ItemStack.EMPTY : remainder;
    }

    public static ItemStack tryAddItemToExistingStack(NonNullList<ItemStack> inventory, int start, int end,
            ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (remainder.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.size() - 1); slot++) {
            ItemStack current = inventory.get(slot);
            if (!current.isEmpty() && HbmItemStackUtil.doesStackDataMatch(current, remainder)) {
                int transfer = Math.min(remainder.getCount(), current.getMaxStackSize() - current.getCount());
                if (transfer > 0) {
                    ItemStack merged = current.copy();
                    merged.grow(transfer);
                    inventory.set(slot, merged);
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return remainder;
    }

    public static boolean tryAddItemToNewSlot(NonNullList<ItemStack> inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (remainder.isEmpty()) {
            return true;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.size() - 1); slot++) {
            if (inventory.get(slot).isEmpty()) {
                inventory.set(slot, remainder);
                return true;
            }
        }
        return false;
    }

    public static ItemStack tryAddItemToInventory(IItemHandler inventory, ItemStack stack) {
        return tryAddItemToInventory(inventory, 0, inventory.getSlots() - 1, stack);
    }

    public static ItemStack tryAddItemToInventory(IItemHandler inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = tryAddItemToExistingStack(inventory, start, end, stack);
        return remainder.isEmpty() ? ItemStack.EMPTY : tryAddItemToNewSlot(inventory, start, end, remainder);
    }

    public static ItemStack tryAddItemToExistingStack(IItemHandler inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (remainder.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            ItemStack current = inventory.getStackInSlot(slot);
            if (!current.isEmpty() && HbmItemStackUtil.doesStackDataMatch(current, remainder)) {
                remainder = inventory.insertItem(slot, remainder, false);
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return remainder;
    }

    public static ItemStack tryAddItemToNewSlot(IItemHandler inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (remainder.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                remainder = inventory.insertItem(slot, remainder, false);
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return remainder;
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, int start, int end,
            List<ItemStack> items) {
        NonNullList<ItemStack> copy = HbmItemStackUtil.carefulCopyList(inventory);
        for (ItemStack item : items) {
            if (!item.isEmpty() && !tryAddItemToInventory(copy, start, end, item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesInventoryHaveSpace(ItemStack[] inventory, int start, int end, ItemStack... items) {
        ItemStack[] copy = HbmItemStackUtil.carefulCopyArray(inventory);
        if (copy == null) {
            return items == null || items.length == 0;
        }
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty() && !tryAddItemToInventory(copy, start, end, item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, int start, int end, List<ItemStack> items) {
        ItemStackHandler copy = copyHandler(inventory);
        for (ItemStack item : items) {
            if (!item.isEmpty() && !tryAddItemToInventory(copy, start, end, item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesInventoryHaveIngredients(ItemStackHandler inventory, int[] slots,
            List<HbmIngredient> ingredients) {
        return tryConsumeIngredients(inventory, slots, ingredients, true);
    }

    public static boolean doesInventoryHaveIngredients(ItemStack[] inventory, int start, int end,
            HbmIngredient... ingredients) {
        return tryConsumeIngredients(inventory, start, end, true, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(IItemHandler inventory, int[] slots,
            List<HbmIngredient> ingredients) {
        return tryConsumeIngredients(inventory, slots, ingredients, true);
    }

    public static boolean tryConsumeIngredients(ItemStack[] inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        MatchPlan plan = matchIngredients(inventory, start, end, ingredients == null ? List.of() : Arrays.asList(ingredients));
        if (plan == null) {
            return false;
        }
        if (!simulate) {
            for (SlotConsumption consumption : plan.consumptions()) {
                ItemStack stack = emptyIfNull(inventory[consumption.slot()]);
                stack.shrink(consumption.count());
                inventory[consumption.slot()] = stack.isEmpty() ? ItemStack.EMPTY : stack;
            }
        }
        return true;
    }

    public static boolean tryConsumeIngredients(ItemStackHandler inventory, int[] slots, List<HbmIngredient> ingredients,
            boolean simulate) {
        return tryConsumeIngredients((IItemHandler) inventory, slots, ingredients, simulate);
    }

    public static boolean tryConsumeIngredients(IItemHandler inventory, int[] slots, List<HbmIngredient> ingredients,
            boolean simulate) {
        MatchPlan plan = matchIngredients(inventory, slots, ingredients);
        if (plan == null) {
            return false;
        }
        if (!simulate) {
            for (SlotConsumption consumption : plan.consumptions()) {
                inventory.extractItem(consumption.slot(), consumption.count(), false);
            }
        }
        return true;
    }

    public static boolean doesPlayerHaveIngredients(Player player, List<HbmIngredient> ingredients,
            boolean shouldRemove) {
        return tryConsumeIngredients(player.getInventory(), ingredients, !shouldRemove);
    }

    public static boolean tryConsumeIngredients(Inventory inventory, List<HbmIngredient> ingredients, boolean simulate) {
        ItemStackHandler handler = new ItemStackHandler(inventory.items.size());
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            handler.setStackInSlot(slot, inventory.items.get(slot).copy());
        }
        int[] slots = range(inventory.items.size());
        MatchPlan plan = matchIngredients(handler, slots, ingredients);
        if (plan == null) {
            return false;
        }
        if (!simulate) {
            for (SlotConsumption consumption : plan.consumptions()) {
                ItemStack stack = inventory.items.get(consumption.slot());
                stack.shrink(consumption.count());
                if (stack.isEmpty()) {
                    inventory.items.set(consumption.slot(), ItemStack.EMPTY);
                }
            }
        }
        return true;
    }

    public static int countIngredientMatches(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean ignoreSize) {
        int count = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && ingredient.test(stack, true)) {
                count += stack.getCount();
            }
        }
        return ignoreSize ? count : count / ingredient.count();
    }

    public static int countIngredientMatches(ItemStack[] inventory, HbmIngredient ingredient, boolean ignoreSize) {
        if (inventory == null || ingredient == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : inventory) {
            if (!emptyIfNull(stack).isEmpty() && ingredient.test(stack, true)) {
                count += stack.getCount();
            }
        }
        return ignoreSize ? count : count / ingredient.count();
    }

    public static int countIngredientMatches(Player player, HbmIngredient ingredient, boolean ignoreSize) {
        return countIngredientMatches(player.getInventory().items, ingredient, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(ItemStack[] inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        if (inventory == null || ingredient == null || countIngredientMatches(inventory, ingredient, ignoreSize) <= 0) {
            return false;
        }
        if (shouldRemove) {
            int required = ignoreSize ? 1 : ingredient.count();
            for (int slot = 0; slot < inventory.length && required > 0; slot++) {
                ItemStack stack = emptyIfNull(inventory[slot]);
                if (!stack.isEmpty() && ingredient.test(stack, true)) {
                    int consumed = Math.min(required, stack.getCount());
                    stack.shrink(consumed);
                    required -= consumed;
                    inventory[slot] = stack.isEmpty() ? ItemStack.EMPTY : stack;
                }
            }
        }
        return true;
    }

    public static boolean doesPlayerHaveIngredient(Player player, HbmIngredient ingredient, boolean shouldRemove,
            boolean ignoreSize) {
        return doesInventoryHaveIngredient(player.getInventory().items, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        if (inventory == null || ingredient == null || countIngredientMatches(inventory, ingredient, ignoreSize) <= 0) {
            return false;
        }
        if (shouldRemove) {
            int required = ignoreSize ? 1 : ingredient.count();
            for (int slot = 0; slot < inventory.size() && required > 0; slot++) {
                ItemStack stack = inventory.get(slot);
                if (!stack.isEmpty() && ingredient.test(stack, true)) {
                    int consumed = Math.min(required, stack.getCount());
                    stack.shrink(consumed);
                    required -= consumed;
                    inventory.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }
        }
        return true;
    }

    public static int countTagMatches(Player player, TagKey<Item> tag) {
        return countTagMatches(player.getInventory().items, tag);
    }

    public static int countTagMatches(NonNullList<ItemStack> inventory, TagKey<Item> tag) {
        if (inventory == null || tag == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.is(tag)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countLegacyOreMatches(Player player, String legacyOreName) {
        return countTagMatches(player, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static boolean hasTagMatches(Player player, TagKey<Item> tag, int count) {
        return countTagMatches(player, tag) >= count;
    }

    public static boolean hasLegacyOreMatches(Player player, String legacyOreName, int count) {
        return hasTagMatches(player, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeTagMatches(Player player, TagKey<Item> tag, int count) {
        int remaining = Math.max(0, count);
        NonNullList<ItemStack> items = player.getInventory().items;
        for (int slot = 0; slot < items.size(); slot++) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty() && stack.is(tag)) {
                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                remaining -= consumed;
                if (stack.isEmpty()) {
                    items.set(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    public static void consumeLegacyOreMatches(Player player, String legacyOreName, int count) {
        consumeTagMatches(player, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static List<List<ItemStack>> extractDisplayStacks(Object object) {
        if (object == null) {
            return List.of();
        }
        if (object instanceof ItemStack stack) {
            return stack.isEmpty() ? List.of() : List.of(List.of(stack.copy()));
        }
        if (object instanceof ItemStack[] stacks) {
            List<List<ItemStack>> result = new ArrayList<>(stacks.length);
            for (ItemStack stack : stacks) {
                result.add(stack == null || stack.isEmpty() ? List.of() : List.of(stack.copy()));
            }
            return List.copyOf(result);
        }
        if (object instanceof ItemStack[][] matrix) {
            List<List<ItemStack>> result = new ArrayList<>(matrix.length);
            for (ItemStack[] row : matrix) {
                List<ItemStack> options = new ArrayList<>();
                if (row != null) {
                    for (ItemStack stack : row) {
                        if (stack != null && !stack.isEmpty()) {
                            options.add(stack.copy());
                        }
                    }
                }
                result.add(List.copyOf(options));
            }
            return List.copyOf(result);
        }
        if (object instanceof HbmIngredient ingredient) {
            return List.of(ingredient.displayStacks());
        }
        if (object instanceof HbmIngredient[] ingredients) {
            List<List<ItemStack>> result = new ArrayList<>(ingredients.length);
            for (HbmIngredient ingredient : ingredients) {
                result.add(ingredient == null ? List.of() : ingredient.displayStacks());
            }
            return List.copyOf(result);
        }
        if (object instanceof HbmItemOutput output) {
            return List.of(output.displayStacks());
        }
        if (object instanceof HbmItemOutput[] outputs) {
            List<List<ItemStack>> result = new ArrayList<>(outputs.length);
            for (HbmItemOutput output : outputs) {
                result.add(output == null ? List.of() : output.displayStacks());
            }
            return List.copyOf(result);
        }
        if (object instanceof Object[] mixed) {
            List<List<ItemStack>> result = new ArrayList<>(mixed.length);
            for (Object entry : mixed) {
                List<List<ItemStack>> extracted = extractDisplayStacks(entry);
                if (extracted.isEmpty()) {
                    result.add(List.of());
                } else {
                    result.add(extracted.get(0));
                    if (extracted.size() > 1) {
                        result.addAll(extracted.subList(1, extracted.size()));
                    }
                }
            }
            return List.copyOf(result);
        }
        return List.of();
    }

    public static ItemStack[][] extractDisplayStackMatrix(Object object) {
        List<List<ItemStack>> rows = extractDisplayStacks(object);
        ItemStack[][] matrix = new ItemStack[rows.size()][];
        for (int row = 0; row < rows.size(); row++) {
            matrix[row] = rows.get(row).stream()
                    .map(ItemStack::copy)
                    .toArray(ItemStack[]::new);
        }
        return matrix;
    }

    private static MatchPlan matchIngredients(IItemHandler inventory, int[] slots, List<HbmIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new MatchPlan(List.of());
        }
        ItemStack[] copy = new ItemStack[inventory.getSlots()];
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            copy[slot] = inventory.getStackInSlot(slot).copy();
        }

        java.util.ArrayList<SlotConsumption> consumptions = new java.util.ArrayList<>();
        for (HbmIngredient ingredient : ingredients) {
            if (ingredient == null) {
                continue;
            }
            int needed = ingredient.count();
            for (int slot : slots) {
                if (slot < 0 || slot >= copy.length) {
                    continue;
                }
                ItemStack current = copy[slot];
                if (!current.isEmpty() && ingredient.test(current, true)) {
                    int consumed = Math.min(needed, current.getCount());
                    current.shrink(consumed);
                    needed -= consumed;
                    consumptions.add(new SlotConsumption(slot, consumed));
                    if (needed <= 0) {
                        break;
                    }
                }
            }
            if (needed > 0) {
                return null;
            }
        }
        return new MatchPlan(List.copyOf(combineConsumptions(consumptions)));
    }

    private static MatchPlan matchIngredients(ItemStack[] inventory, int start, int end, List<HbmIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new MatchPlan(List.of());
        }
        if (inventory == null) {
            return null;
        }
        ItemStack[] copy = HbmItemStackUtil.carefulCopyArray(inventory);
        ArrayList<SlotConsumption> consumptions = new ArrayList<>();
        for (HbmIngredient ingredient : ingredients) {
            if (ingredient == null) {
                continue;
            }
            int needed = ingredient.count();
            for (int slot = Math.max(0, start); slot <= Math.min(end, copy.length - 1); slot++) {
                ItemStack current = emptyIfNull(copy[slot]);
                if (!current.isEmpty() && ingredient.test(current, true)) {
                    int consumed = Math.min(needed, current.getCount());
                    current.shrink(consumed);
                    copy[slot] = current.isEmpty() ? ItemStack.EMPTY : current;
                    needed -= consumed;
                    consumptions.add(new SlotConsumption(slot, consumed));
                    if (needed <= 0) {
                        break;
                    }
                }
            }
            if (needed > 0) {
                return null;
            }
        }
        return new MatchPlan(List.copyOf(combineConsumptions(consumptions)));
    }

    private static List<SlotConsumption> combineConsumptions(List<SlotConsumption> consumptions) {
        java.util.Map<Integer, Integer> combined = new java.util.LinkedHashMap<>();
        for (SlotConsumption consumption : consumptions) {
            combined.merge(consumption.slot(), consumption.count(), Integer::sum);
        }
        return combined.entrySet().stream()
                .map(entry -> new SlotConsumption(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static ItemStackHandler copyHandler(IItemHandler inventory) {
        ItemStackHandler copy = new ItemStackHandler(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            copy.setStackInSlot(slot, inventory.getStackInSlot(slot).copy());
        }
        return copy;
    }

    private static int[] range(int size) {
        int[] slots = new int[size];
        Arrays.setAll(slots, index -> index);
        return slots;
    }

    private static ItemStack emptyIfNull(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private record SlotConsumption(int slot, int count) {
    }

    private record MatchPlan(List<SlotConsumption> consumptions) {
    }
}
