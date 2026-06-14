package com.hbm.ntm.util;

import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public final class HbmInventoryUtil {
    private HbmInventoryUtil() {
    }

    public static boolean doesStackDataMatch(ItemStack first, ItemStack second) {
        return HbmItemStackUtil.doesStackDataMatch(first, second);
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
        if (remainder.isEmpty()) {
            return true;
        }
        if (inventory == null) {
            return false;
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
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        return tryAddItemToInventory(inventory, 0, inventory.size() - 1, stack);
    }

    public static ItemStack tryAddItemToInventory(NonNullList<ItemStack> inventory, int start, int end, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        ItemStack remainder = tryAddItemToExistingStack(inventory, start, end, stack);
        return remainder.isEmpty() || tryAddItemToNewSlot(inventory, start, end, remainder) ? ItemStack.EMPTY : remainder;
    }

    public static ItemStack tryAddItemToExistingStack(NonNullList<ItemStack> inventory, int start, int end,
            ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
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
        if (inventory == null) {
            return false;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.size() - 1); slot++) {
            if (inventory.get(slot).isEmpty()) {
                inventory.set(slot, remainder);
                return true;
            }
        }
        return false;
    }

    public static ItemStack tryAddItemToInventory(Player player, ItemStack stack) {
        return player == null ? HbmItemStackUtil.carefulCopy(stack)
                : tryAddItemToInventory(player.getInventory(), stack);
    }

    public static ItemStack tryAddItemToInventory(Inventory inventory, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        return tryAddItemToInventory(inventory, 0, inventory.items.size() - 1, stack);
    }

    public static ItemStack tryAddItemToInventory(Inventory inventory, int start, int end, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        ItemStack remainder = tryAddItemToExistingStack(inventory, start, end, stack);
        return remainder.isEmpty() || tryAddItemToNewSlot(inventory, start, end, remainder) ? ItemStack.EMPTY : remainder;
    }

    public static ItemStack tryAddItemToExistingStack(Inventory inventory, int start, int end, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        ItemStack input = HbmItemStackUtil.carefulCopy(stack);
        ItemStack remainder = tryAddItemToExistingStack(inventory.items, start, end, input);
        if (remainder.getCount() != input.getCount()) {
            inventory.setChanged();
        }
        return remainder;
    }

    public static boolean tryAddItemToNewSlot(Inventory inventory, int start, int end, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        if (inventory == null) {
            return false;
        }
        boolean added = tryAddItemToNewSlot(inventory.items, start, end, stack);
        if (added) {
            inventory.setChanged();
        }
        return added;
    }

    public static ItemStack tryAddItemToInventory(IItemHandler inventory, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        return tryAddItemToInventory(inventory, 0, inventory.getSlots() - 1, stack);
    }

    public static ItemStack tryAddItemToInventory(IItemHandler inventory, int start, int end, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        ItemStack remainder = tryAddItemToExistingStack(inventory, start, end, stack);
        return remainder.isEmpty() ? ItemStack.EMPTY : tryAddItemToNewSlot(inventory, start, end, remainder);
    }

    public static ItemStack tryAddItemToExistingStack(IItemHandler inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
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
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
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

    public static boolean doesHandlerHaveSpaceUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack... items) {
        return doesHandlerHaveSpaceUnchecked(inventory, start, end, items == null ? List.of() : Arrays.asList(items));
    }

    public static boolean doesHandlerHaveSpaceUnchecked(ItemStackHandler inventory, int start, int end,
            List<ItemStack> items) {
        if (inventory == null) {
            return items == null || items.isEmpty();
        }
        ItemStack[] copy = HbmItemStackUtil.carefulCopyArray(inventory);
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty()
                    && !tryAddItemToHandlerCopyUnchecked(inventory, copy, start, end, item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack tryAddItemToHandlerUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack stack) {
        ItemStack remainder = tryAddItemToExistingStacksUnchecked(inventory, start, end, stack);
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                int transfer = Math.min(remainder.getCount(), slotLimit(inventory, slot, remainder));
                if (transfer > 0) {
                    inventory.setStackInSlot(slot, HbmItemStackUtil.carefulCopyWithSize(remainder, transfer));
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return remainder;
    }

    public static ItemStack tryAddItemToExistingStacksUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            ItemStack current = inventory.getStackInSlot(slot);
            if (!current.isEmpty() && HbmItemStackUtil.doesStackDataMatch(current, remainder)) {
                int transfer = Math.min(remainder.getCount(), slotSpace(inventory, slot, current));
                if (transfer > 0) {
                    ItemStack merged = current.copy();
                    merged.grow(transfer);
                    inventory.setStackInSlot(slot, merged);
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return remainder;
    }

    public static ItemStack tryAddItemToFirstNewSlotUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            if (inventory.getStackInSlot(slot).isEmpty()) {
                int transfer = Math.min(remainder.getCount(), slotLimit(inventory, slot, remainder));
                if (transfer > 0) {
                    inventory.setStackInSlot(slot, HbmItemStackUtil.carefulCopyWithSize(remainder, transfer));
                    remainder.shrink(transfer);
                    return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
                }
            }
        }
        return remainder;
    }

    public static boolean moveSingleItemFromHandlerToHandler(ItemStackHandler source, int sourceStart, int sourceEnd,
            IItemHandler target) {
        if (source == null || target == null) {
            return false;
        }
        for (int slot = Math.max(0, sourceStart); slot <= Math.min(sourceEnd, source.getSlots() - 1); slot++) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack single = HbmItemStackUtil.carefulCopyWithSize(stack, 1);
            if (ItemHandlerHelper.insertItemStacked(target, single, true).isEmpty()
                    && ItemHandlerHelper.insertItemStacked(target, single, false).isEmpty()) {
                removeOneFromHandlerSlot(source, slot, stack);
                return true;
            }
        }
        return false;
    }

    public static boolean moveSingleItemFromHandlerToContainer(ItemStackHandler source, int sourceStart, int sourceEnd,
            Container target) {
        if (source == null || target == null) {
            return false;
        }
        for (int slot = Math.max(0, sourceStart); slot <= Math.min(sourceEnd, source.getSlots() - 1); slot++) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack single = HbmItemStackUtil.carefulCopyWithSize(stack, 1);
            for (int targetSlot = 0; targetSlot < target.getContainerSize(); targetSlot++) {
                ItemStack current = target.getItem(targetSlot);
                if (!current.isEmpty() && HbmItemStackUtil.doesStackDataMatch(current, single)
                        && current.getCount() < current.getMaxStackSize()) {
                    current.grow(1);
                    target.setItem(targetSlot, current);
                    target.setChanged();
                    removeOneFromHandlerSlot(source, slot, stack);
                    return true;
                }
            }
        }

        for (int slot = Math.max(0, sourceStart); slot <= Math.min(sourceEnd, source.getSlots() - 1); slot++) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack single = HbmItemStackUtil.carefulCopyWithSize(stack, 1);
            for (int targetSlot = 0; targetSlot < target.getContainerSize(); targetSlot++) {
                if (target.getItem(targetSlot).isEmpty() && target.canPlaceItem(targetSlot, single)) {
                    target.setItem(targetSlot, single);
                    target.setChanged();
                    removeOneFromHandlerSlot(source, slot, stack);
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack tryAddItemToInventory(Container inventory, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        return tryAddItemToInventory(inventory, 0, inventory.getContainerSize() - 1, stack);
    }

    public static ItemStack tryAddItemToInventory(Container inventory, int start, int end, ItemStack stack) {
        if (inventory == null) {
            return HbmItemStackUtil.carefulCopy(stack);
        }
        ItemStack remainder = tryAddItemToExistingStack(inventory, start, end, stack);
        return remainder.isEmpty() || tryAddItemToNewSlot(inventory, start, end, remainder) ? ItemStack.EMPTY : remainder;
    }

    public static ItemStack tryAddItemToExistingStack(Container inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (inventory == null || remainder.isEmpty()) {
            return remainder;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getContainerSize() - 1); slot++) {
            ItemStack current = inventory.getItem(slot);
            if (!current.isEmpty() && inventory.canPlaceItem(slot, remainder)
                    && HbmItemStackUtil.doesStackDataMatch(current, remainder)) {
                int transfer = Math.min(remainder.getCount(),
                        Math.min(inventory.getMaxStackSize(), current.getMaxStackSize()) - current.getCount());
                if (transfer > 0) {
                    current.grow(transfer);
                    inventory.setItem(slot, current);
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        inventory.setChanged();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        inventory.setChanged();
        return remainder;
    }

    public static boolean tryAddItemToNewSlot(Container inventory, int start, int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (remainder.isEmpty()) {
            return true;
        }
        if (inventory == null) {
            return false;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getContainerSize() - 1); slot++) {
            if (inventory.getItem(slot).isEmpty() && inventory.canPlaceItem(slot, remainder)) {
                inventory.setItem(slot, remainder);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, int start, int end,
            List<ItemStack> items) {
        if (inventory == null) {
            return items == null || items.isEmpty();
        }
        NonNullList<ItemStack> copy = HbmItemStackUtil.carefulCopyList(inventory);
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty() && !tryAddItemToInventory(copy, start, end, item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, int start, int end,
            ItemStack... items) {
        return doesInventoryHaveSpace(inventory, start, end, items == null ? List.of() : Arrays.asList(items));
    }

    public static boolean doesInventoryHaveSpace(Player player, ItemStack... items) {
        return player != null && doesInventoryHaveSpace(player.getInventory(), items);
    }

    public static boolean doesInventoryHaveSpace(Player player, List<ItemStack> items) {
        return player != null && doesInventoryHaveSpace(player.getInventory(), items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, ItemStack... items) {
        return inventory != null && doesInventoryHaveSpace(inventory, 0, inventory.items.size() - 1, items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, List<ItemStack> items) {
        return inventory != null && doesInventoryHaveSpace(inventory, 0, inventory.items.size() - 1, items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, int start, int end, ItemStack... items) {
        return inventory != null && doesInventoryHaveSpace(inventory.items, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, int start, int end, List<ItemStack> items) {
        return inventory != null && doesInventoryHaveSpace(inventory.items, start, end, items);
    }

    public static boolean doesPlayerInventoryHaveSpace(Player player, ItemStack... items) {
        return doesInventoryHaveSpace(player, items);
    }

    public static boolean doesPlayerInventoryHaveSpace(Player player, List<ItemStack> items) {
        return doesInventoryHaveSpace(player, items);
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

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, ItemStack... items) {
        return inventory != null && doesInventoryHaveSpace(inventory, 0, inventory.length - 1, items);
    }

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, List<ItemStack> items) {
        return inventory != null && doesArrayHaveSpace(inventory, 0, inventory.length - 1, items);
    }

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, int start, int end, ItemStack[] items) {
        return doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, int start, int end, List<ItemStack> items) {
        return doesInventoryHaveSpace(inventory, start, end, items == null ? new ItemStack[0]
                : items.toArray(ItemStack[]::new));
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, int start, int end, List<ItemStack> items) {
        if (inventory == null) {
            return items == null || items.isEmpty();
        }
        ItemStackHandler copy = copyHandler(inventory);
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty() && !tryAddItemToInventory(copy, start, end, item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, int start, int end, ItemStack... items) {
        return doesInventoryHaveSpace(inventory, start, end, items == null ? List.of() : Arrays.asList(items));
    }

    public static boolean doesInventoryHaveSpace(Container inventory, int start, int end, List<ItemStack> items) {
        if (inventory == null) {
            return items == null || items.isEmpty();
        }
        ItemStack[] copy = copyContainer(inventory);
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty()
                    && !tryAddItemToContainerCopy(inventory, copy, start, end, item).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean doesInventoryHaveSpace(Container inventory, int start, int end, ItemStack... items) {
        return doesInventoryHaveSpace(inventory, start, end, items == null ? List.of() : Arrays.asList(items));
    }

    public static boolean doesInventoryHaveIngredients(ItemStackHandler inventory, int[] slots,
            List<HbmIngredient> ingredients) {
        return tryConsumeIngredients(inventory, slots, ingredients, true);
    }

    public static boolean doesInventoryHaveIngredients(ItemStack[] inventory, int start, int end,
            HbmIngredient... ingredients) {
        return tryConsumeIngredients(inventory, start, end, true, ingredients);
    }

    public static boolean doesArrayHaveIngredients(ItemStack[] inventory, int start, int end,
            HbmIngredient... ingredients) {
        return doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesArrayHaveIngredients(ItemStack[] inventory, HbmIngredient... ingredients) {
        return inventory != null && doesArrayHaveIngredients(inventory, 0, inventory.length - 1, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(IItemHandler inventory, int[] slots,
            List<HbmIngredient> ingredients) {
        return tryConsumeIngredients(inventory, slots, ingredients, true);
    }

    public static boolean doesInventoryHaveIngredients(IItemHandler inventory, int start, int end,
            HbmIngredient... ingredients) {
        return tryConsumeIngredients(inventory, start, end, true, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(Container inventory, int start, int end,
            HbmIngredient... ingredients) {
        return tryConsumeIngredients(inventory, start, end, true, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(Inventory inventory, List<HbmIngredient> ingredients) {
        return tryConsumeIngredients(inventory, ingredients, true);
    }

    public static boolean doesInventoryHaveIngredients(Inventory inventory, int start, int end,
            HbmIngredient... ingredients) {
        return tryConsumeIngredients(inventory, start, end, true, ingredients);
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

    public static boolean tryConsumeAStack(ItemStack[] inventory, int start, int end, HbmIngredient ingredient) {
        return ingredient == null || tryConsumeIngredients(inventory, start, end, false, ingredient);
    }

    public static boolean tryConsumeAStack(IItemHandler inventory, int start, int end, HbmIngredient ingredient) {
        return ingredient == null || tryConsumeIngredients(inventory, start, end, false, ingredient);
    }

    public static boolean tryConsumeAStack(Container inventory, int start, int end, HbmIngredient ingredient) {
        return ingredient == null || tryConsumeIngredients(inventory, start, end, false, ingredient);
    }

    public static boolean tryConsumeAStack(Inventory inventory, HbmIngredient ingredient) {
        return ingredient == null || tryConsumeIngredients(inventory, List.of(ingredient), false);
    }

    public static boolean tryConsumeAStack(Inventory inventory, int start, int end, HbmIngredient ingredient) {
        return ingredient == null || tryConsumeIngredients(inventory, start, end, false, ingredient);
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

    public static boolean tryConsumeIngredients(IItemHandler inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        if (inventory == null) {
            return ingredients == null || ingredients.length == 0;
        }
        return tryConsumeIngredients(inventory, rangeInclusive(start, end, inventory.getSlots()),
                ingredients == null ? List.of() : Arrays.asList(ingredients), simulate);
    }

    public static boolean tryConsumeIngredients(Container inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        MatchPlan plan = matchIngredients(inventory, start, end, ingredients == null ? List.of() : Arrays.asList(ingredients));
        if (plan == null) {
            return false;
        }
        if (!simulate) {
            for (SlotConsumption consumption : plan.consumptions()) {
                ItemStack stack = inventory.getItem(consumption.slot());
                stack.shrink(consumption.count());
                inventory.setItem(consumption.slot(), stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
            inventory.setChanged();
        }
        return true;
    }

    public static boolean doesPlayerHaveIngredients(Player player, List<HbmIngredient> ingredients,
            boolean shouldRemove) {
        if (player == null) {
            return ingredients == null || ingredients.isEmpty();
        }
        return tryConsumeIngredients(player.getInventory(), ingredients, !shouldRemove);
    }

    public static boolean doesPlayerHaveAStacks(Player player, List<HbmIngredient> ingredients,
            boolean shouldRemove) {
        return doesPlayerHaveIngredients(player, ingredients, shouldRemove);
    }

    public static boolean tryConsumeIngredients(Inventory inventory, List<HbmIngredient> ingredients, boolean simulate) {
        return tryConsumeIngredients(inventory, 0, inventory == null ? -1 : inventory.items.size() - 1, ingredients,
                simulate);
    }

    public static boolean tryConsumeIngredients(Inventory inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        return tryConsumeIngredients(inventory, start, end, ingredients == null ? List.of() : Arrays.asList(ingredients),
                simulate);
    }

    public static boolean tryConsumeIngredients(Inventory inventory, int start, int end,
            List<HbmIngredient> ingredients, boolean simulate) {
        if (inventory == null) {
            return ingredients == null || ingredients.isEmpty();
        }
        ItemStackHandler handler = new ItemStackHandler(inventory.items.size());
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            handler.setStackInSlot(slot, inventory.items.get(slot).copy());
        }
        int[] slots = rangeInclusive(start, end, inventory.items.size());
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
            inventory.setChanged();
        }
        return true;
    }

    public static int countIngredientMatches(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean ignoreSize) {
        return inventory == null ? 0 : countIngredientMatches(inventory, 0, inventory.size() - 1, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(NonNullList<ItemStack> inventory, int start, int end,
            HbmIngredient ingredient, boolean ignoreSize) {
        if (inventory == null || ingredient == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.size() - 1); slot++) {
            ItemStack stack = inventory.get(slot);
            if (!stack.isEmpty() && ingredient.test(stack, true)) {
                count += stack.getCount();
            }
        }
        return ignoreSize ? count : count / ingredient.count();
    }

    public static int countIngredientMatches(ItemStack[] inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return inventory == null ? 0 : countIngredientMatches(inventory, 0, inventory.length - 1, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(ItemStack[] inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        if (inventory == null || ingredient == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.length - 1); slot++) {
            ItemStack stack = inventory[slot];
            if (!emptyIfNull(stack).isEmpty() && ingredient.test(stack, true)) {
                count += stack.getCount();
            }
        }
        return ignoreSize ? count : count / ingredient.count();
    }

    public static int countAStackMatches(ItemStack[] inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(ItemStack[] inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Container inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return inventory == null ? 0 : countIngredientMatches(inventory, 0, inventory.getContainerSize() - 1,
                ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Container inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        if (inventory == null || ingredient == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getContainerSize() - 1); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && ingredient.test(stack, true)) {
                count += stack.getCount();
            }
        }
        return ignoreSize ? count : count / ingredient.count();
    }

    public static int countAStackMatches(Container inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Container inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(IItemHandler inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return inventory == null ? 0 : countIngredientMatches(inventory, 0, inventory.getSlots() - 1, ingredient,
                ignoreSize);
    }

    public static int countIngredientMatches(IItemHandler inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        if (inventory == null || ingredient == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && ingredient.test(stack, true)) {
                count += stack.getCount();
            }
        }
        return ignoreSize ? count : count / ingredient.count();
    }

    public static int countAStackMatches(IItemHandler inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(IItemHandler inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Player player, HbmIngredient ingredient, boolean ignoreSize) {
        return player == null ? 0 : countIngredientMatches(player.getInventory(), ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Inventory inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return inventory == null ? 0 : countIngredientMatches(inventory, 0, inventory.items.size() - 1, ingredient,
                ignoreSize);
    }

    public static int countIngredientMatches(Inventory inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return inventory == null ? 0 : countIngredientMatches(inventory.items, start, end, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Player player, HbmIngredient ingredient, boolean ignoreSize) {
        return countIngredientMatches(player, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Inventory inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Inventory inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(ItemStack[] inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return inventory != null && doesInventoryHaveIngredient(inventory, 0, inventory.length - 1, ingredient,
                shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(ItemStack[] inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        if (inventory == null || ingredient == null
                || countIngredientMatches(inventory, start, end, ingredient, ignoreSize) <= 0) {
            return false;
        }
        if (shouldRemove) {
            int required = ignoreSize ? 1 : ingredient.count();
            for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.length - 1) && required > 0; slot++) {
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
        return player != null && doesInventoryHaveIngredient(player.getInventory(), ingredient, shouldRemove,
                ignoreSize);
    }

    public static boolean doesPlayerHaveAStack(Player player, HbmIngredient ingredient, boolean shouldRemove,
            boolean ignoreSize) {
        return doesPlayerHaveIngredient(player, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Inventory inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return inventory != null && doesInventoryHaveIngredient(inventory, 0, inventory.items.size() - 1, ingredient,
                shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Inventory inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        if (inventory == null) {
            return false;
        }
        boolean matched = doesInventoryHaveIngredient(inventory.items, start, end, ingredient, shouldRemove,
                ignoreSize);
        if (matched && shouldRemove) {
            inventory.setChanged();
        }
        return matched;
    }

    public static boolean doesInventoryHaveAStack(Inventory inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(Inventory inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return inventory != null && doesInventoryHaveIngredient(inventory, 0, inventory.size() - 1, ingredient,
                shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(NonNullList<ItemStack> inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        if (inventory == null || ingredient == null
                || countIngredientMatches(inventory, start, end, ingredient, ignoreSize) <= 0) {
            return false;
        }
        if (shouldRemove) {
            int required = ignoreSize ? 1 : ingredient.count();
            for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.size() - 1) && required > 0; slot++) {
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

    public static boolean doesInventoryHaveAStack(ItemStack[] inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(ItemStack[] inventory, int start, int end, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(NonNullList<ItemStack> inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Container inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return inventory != null && doesInventoryHaveIngredient(inventory, 0, inventory.getContainerSize() - 1,
                ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Container inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        if (inventory == null || ingredient == null
                || countIngredientMatches(inventory, start, end, ingredient, ignoreSize) <= 0) {
            return false;
        }
        if (shouldRemove) {
            int required = ignoreSize ? 1 : ingredient.count();
            for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getContainerSize() - 1)
                    && required > 0; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty() && ingredient.test(stack, true)) {
                    int consumed = Math.min(required, stack.getCount());
                    stack.shrink(consumed);
                    required -= consumed;
                    inventory.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }
            inventory.setChanged();
        }
        return true;
    }

    public static boolean doesInventoryHaveAStack(Container inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(Container inventory, int start, int end, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(IItemHandler inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return inventory != null && doesInventoryHaveIngredient(inventory, 0, inventory.getSlots() - 1, ingredient,
                shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(IItemHandler inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        if (inventory == null || ingredient == null
                || countIngredientMatches(inventory, start, end, ingredient, ignoreSize) <= 0) {
            return false;
        }
        if (shouldRemove) {
            int required = ignoreSize ? 1 : ingredient.count();
            int[] slots = rangeInclusive(start, end, inventory.getSlots());
            return tryConsumeIngredients(inventory, slots, List.of(withCount(ingredient, required)), false);
        }
        return true;
    }

    public static boolean doesInventoryHaveAStack(IItemHandler inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(IItemHandler inventory, int start, int end, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static int countTagMatches(Player player, TagKey<Item> tag) {
        return player == null ? 0 : countTagMatches(player.getInventory(), tag);
    }

    public static int countTagMatches(Inventory inventory, TagKey<Item> tag) {
        return inventory == null ? 0 : countTagMatches(inventory, 0, inventory.items.size() - 1, tag);
    }

    public static int countTagMatches(Inventory inventory, int start, int end, TagKey<Item> tag) {
        return inventory == null ? 0 : countTagMatches(inventory.items, start, end, tag);
    }

    public static int countTagMatches(NonNullList<ItemStack> inventory, TagKey<Item> tag) {
        return inventory == null ? 0 : countTagMatches(inventory, 0, inventory.size() - 1, tag);
    }

    public static int countTagMatches(NonNullList<ItemStack> inventory, int start, int end, TagKey<Item> tag) {
        if (inventory == null || tag == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.size() - 1); slot++) {
            ItemStack stack = inventory.get(slot);
            if (!stack.isEmpty() && stack.is(tag)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countTagMatches(ItemStack[] inventory, TagKey<Item> tag) {
        return inventory == null ? 0 : countTagMatches(inventory, 0, inventory.length - 1, tag);
    }

    public static int countTagMatches(ItemStack[] inventory, int start, int end, TagKey<Item> tag) {
        if (inventory == null || tag == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.length - 1); slot++) {
            ItemStack stack = inventory[slot];
            if (!emptyIfNull(stack).isEmpty() && stack.is(tag)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countTagMatches(IItemHandler inventory, TagKey<Item> tag) {
        return inventory == null ? 0 : countTagMatches(inventory, 0, inventory.getSlots() - 1, tag);
    }

    public static int countTagMatches(IItemHandler inventory, int start, int end, TagKey<Item> tag) {
        if (inventory == null || tag == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.is(tag)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countTagMatches(Container inventory, TagKey<Item> tag) {
        return inventory == null ? 0 : countTagMatches(inventory, 0, inventory.getContainerSize() - 1, tag);
    }

    public static int countTagMatches(Container inventory, int start, int end, TagKey<Item> tag) {
        if (inventory == null || tag == null) {
            return 0;
        }
        int count = 0;
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getContainerSize() - 1); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.is(tag)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countLegacyOreMatches(Player player, String legacyOreName) {
        return countTagMatches(player, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(Player player, String legacyOreName) {
        return countLegacyOreMatches(player, legacyOreName);
    }

    public static int countLegacyOreMatches(Inventory inventory, String legacyOreName) {
        return countTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(Inventory inventory, String legacyOreName) {
        return countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(Inventory inventory, int start, int end, String legacyOreName) {
        return countTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(Inventory inventory, int start, int end, String legacyOreName) {
        return countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(ItemStack[] inventory, String legacyOreName) {
        return countTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(ItemStack[] inventory, String legacyOreName) {
        return countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(ItemStack[] inventory, int start, int end, String legacyOreName) {
        return countTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(ItemStack[] inventory, int start, int end, String legacyOreName) {
        return countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(NonNullList<ItemStack> inventory, String legacyOreName) {
        return countTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(NonNullList<ItemStack> inventory, String legacyOreName) {
        return countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName) {
        return countTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName) {
        return countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(IItemHandler inventory, String legacyOreName) {
        return countTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(IItemHandler inventory, String legacyOreName) {
        return countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(IItemHandler inventory, int start, int end, String legacyOreName) {
        return countTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(IItemHandler inventory, int start, int end, String legacyOreName) {
        return countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(Container inventory, String legacyOreName) {
        return countTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(Container inventory, String legacyOreName) {
        return countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(Container inventory, int start, int end, String legacyOreName) {
        return countTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName));
    }

    public static int countOreDictMatches(Container inventory, int start, int end, String legacyOreName) {
        return countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static boolean hasTagMatches(Player player, TagKey<Item> tag, int count) {
        return countTagMatches(player, tag) >= count;
    }

    public static boolean hasTagMatches(Inventory inventory, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, tag) >= count;
    }

    public static boolean hasTagMatches(Inventory inventory, int start, int end, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, start, end, tag) >= count;
    }

    public static boolean hasTagMatches(ItemStack[] inventory, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, tag) >= count;
    }

    public static boolean hasTagMatches(ItemStack[] inventory, int start, int end, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, start, end, tag) >= count;
    }

    public static boolean hasTagMatches(NonNullList<ItemStack> inventory, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, tag) >= count;
    }

    public static boolean hasTagMatches(NonNullList<ItemStack> inventory, int start, int end, TagKey<Item> tag,
            int count) {
        return countTagMatches(inventory, start, end, tag) >= count;
    }

    public static boolean hasTagMatches(IItemHandler inventory, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, tag) >= count;
    }

    public static boolean hasTagMatches(IItemHandler inventory, int start, int end, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, start, end, tag) >= count;
    }

    public static boolean hasTagMatches(Container inventory, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, tag) >= count;
    }

    public static boolean hasTagMatches(Container inventory, int start, int end, TagKey<Item> tag, int count) {
        return countTagMatches(inventory, start, end, tag) >= count;
    }

    public static boolean hasLegacyOreMatches(Player player, String legacyOreName, int count) {
        return hasTagMatches(player, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(Player player, String legacyOreName, int count) {
        return hasLegacyOreMatches(player, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Inventory inventory, String legacyOreName, int count) {
        return hasTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(Inventory inventory, String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Inventory inventory, int start, int end, String legacyOreName,
            int count) {
        return hasTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(Inventory inventory, int start, int end, String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(ItemStack[] inventory, String legacyOreName, int count) {
        return hasTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(ItemStack[] inventory, String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        return hasTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        return hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        return hasTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName, int count) {
        return hasTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(IItemHandler inventory, String legacyOreName, int count) {
        return hasTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(IItemHandler inventory, String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        return hasTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        return hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Container inventory, String legacyOreName, int count) {
        return hasTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(Container inventory, String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Container inventory, int start, int end, String legacyOreName, int count) {
        return hasTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static boolean hasOreDictMatches(Container inventory, int start, int end, String legacyOreName, int count) {
        return hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeTagMatches(Player player, TagKey<Item> tag, int count) {
        if (player == null) {
            return;
        }
        consumeTagMatches(player.getInventory(), tag, count);
    }

    public static void consumeTagMatches(Inventory inventory, TagKey<Item> tag, int count) {
        consumeTagMatches(inventory, 0, inventory == null ? -1 : inventory.items.size() - 1, tag, count);
    }

    public static void consumeTagMatches(Inventory inventory, int start, int end, TagKey<Item> tag, int count) {
        if (inventory == null) {
            return;
        }
        int before = countTagMatches(inventory, start, end, tag);
        consumeTagMatches(inventory.items, start, end, tag, count);
        if (before != countTagMatches(inventory, start, end, tag)) {
            inventory.setChanged();
        }
    }

    public static void consumeTagMatches(NonNullList<ItemStack> items, TagKey<Item> tag, int count) {
        if (items == null) {
            return;
        }
        consumeTagMatches(items, 0, items.size() - 1, tag, count);
    }

    public static void consumeTagMatches(NonNullList<ItemStack> items, int start, int end, TagKey<Item> tag,
            int count) {
        int remaining = Math.max(0, count);
        if (items == null || tag == null) {
            return;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, items.size() - 1); slot++) {
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

    public static void consumeTagMatches(ItemStack[] items, TagKey<Item> tag, int count) {
        if (items == null) {
            return;
        }
        consumeTagMatches(items, 0, items.length - 1, tag, count);
    }

    public static void consumeTagMatches(ItemStack[] items, int start, int end, TagKey<Item> tag, int count) {
        int remaining = Math.max(0, count);
        if (items == null || tag == null) {
            return;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, items.length - 1); slot++) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = emptyIfNull(items[slot]);
            if (!stack.isEmpty() && stack.is(tag)) {
                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                remaining -= consumed;
                items[slot] = stack.isEmpty() ? ItemStack.EMPTY : stack;
            }
        }
    }

    public static void consumeTagMatches(IItemHandler inventory, TagKey<Item> tag, int count) {
        if (inventory == null) {
            return;
        }
        consumeTagMatches(inventory, 0, inventory.getSlots() - 1, tag, count);
    }

    public static void consumeTagMatches(IItemHandler inventory, int start, int end, TagKey<Item> tag, int count) {
        int remaining = Math.max(0, count);
        if (inventory == null || tag == null) {
            return;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getSlots() - 1); slot++) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.is(tag)) {
                int consumed = Math.min(remaining, stack.getCount());
                ItemStack extracted = inventory.extractItem(slot, consumed, false);
                remaining -= extracted.getCount();
            }
        }
    }

    public static void consumeTagMatches(Container inventory, TagKey<Item> tag, int count) {
        if (inventory == null) {
            return;
        }
        consumeTagMatches(inventory, 0, inventory.getContainerSize() - 1, tag, count);
    }

    public static void consumeTagMatches(Container inventory, int start, int end, TagKey<Item> tag, int count) {
        int remaining = Math.max(0, count);
        if (inventory == null || tag == null) {
            return;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, inventory.getContainerSize() - 1); slot++) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.is(tag)) {
                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                remaining -= consumed;
                inventory.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
        }
        inventory.setChanged();
    }

    public static void consumeLegacyOreMatches(Player player, String legacyOreName, int count) {
        consumeTagMatches(player, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(Player player, String legacyOreName, int count) {
        consumeLegacyOreMatches(player, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Inventory inventory, String legacyOreName, int count) {
        consumeTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(Inventory inventory, String legacyOreName, int count) {
        consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Inventory inventory, int start, int end, String legacyOreName,
            int count) {
        consumeTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(Inventory inventory, int start, int end, String legacyOreName,
            int count) {
        consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(ItemStack[] inventory, String legacyOreName, int count) {
        consumeTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(ItemStack[] inventory, String legacyOreName, int count) {
        consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        consumeTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        consumeTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName, int count) {
        consumeTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName, int count) {
        consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(IItemHandler inventory, String legacyOreName, int count) {
        consumeTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(IItemHandler inventory, String legacyOreName, int count) {
        consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        consumeTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Container inventory, String legacyOreName, int count) {
        consumeTagMatches(inventory, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(Container inventory, String legacyOreName, int count) {
        consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Container inventory, int start, int end, String legacyOreName,
            int count) {
        consumeTagMatches(inventory, start, end, LegacyOreDictionaryMappings.itemTag(legacyOreName), count);
    }

    public static void consumeOreDictMatches(Container inventory, int start, int end, String legacyOreName,
            int count) {
        consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static List<ItemStack> clearToDrops(NonNullList<ItemStack> items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(ItemStack[] items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(Container items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(IItemHandler items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(ItemStackHandler items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static void spillItems(Level level, BlockPos pos, IItemHandler items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, IItemHandler items) {
        HbmItemStackUtil.spillItems(level, pos, items);
    }

    public static void spillItems(Level level, BlockPos pos, NonNullList<ItemStack> items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, NonNullList<ItemStack> items) {
        HbmItemStackUtil.spillItems(level, pos, items);
    }

    public static void spillItems(Level level, BlockPos pos, ItemStack[] items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, ItemStack[] items) {
        HbmItemStackUtil.spillItems(level, pos, items);
    }

    public static void spillItems(Level level, BlockPos pos, Container items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, Container items) {
        HbmItemStackUtil.spillItems(level, pos, items);
    }

    public static void spillStack(Level level, BlockPos pos, ItemStack stack, RandomSource random) {
        HbmItemStackUtil.spillStack(level, pos, stack, random);
    }

    public static void spillStack(Level level, BlockPos pos, ItemStack stack) {
        HbmItemStackUtil.spillStack(level, pos, stack);
    }

    public static void dropStack(Level level, double x, double y, double z, ItemStack stack) {
        HbmItemStackUtil.dropStack(level, x, y, z, stack);
    }

    public static void dropStack(Level level, BlockPos pos, ItemStack stack) {
        HbmItemStackUtil.dropStack(level, pos, stack);
    }

    public static void dropStacks(Level level, double x, double y, double z, Iterable<ItemStack> stacks) {
        HbmItemStackUtil.dropStacks(level, x, y, z, stacks);
    }

    public static void dropStacks(Level level, BlockPos pos, Iterable<ItemStack> stacks) {
        HbmItemStackUtil.dropStacks(level, pos, stacks);
    }

    public static void dropStacks(Level level, double x, double y, double z, ItemStack[] stacks) {
        HbmItemStackUtil.dropStacks(level, x, y, z, stacks);
    }

    public static void dropStacks(Level level, BlockPos pos, ItemStack[] stacks) {
        HbmItemStackUtil.dropStacks(level, pos, stacks);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack) {
        return HbmItemStackUtil.giveOrDrop(player, stack);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, boolean throwRandomly) {
        return HbmItemStackUtil.giveOrDrop(player, stack, throwRandomly);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, Level fallbackLevel, double x, double y,
            double z) {
        return HbmItemStackUtil.giveOrDrop(player, stack, fallbackLevel, x, y, z);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, Level fallbackLevel, BlockPos pos) {
        return HbmItemStackUtil.giveOrDrop(player, stack, fallbackLevel, pos);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks, boolean throwRandomly) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks, throwRandomly);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks, boolean throwRandomly) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks, throwRandomly);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks, Level fallbackLevel, double x,
            double y, double z) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks, fallbackLevel, x, y, z);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks, Level fallbackLevel, BlockPos pos) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks, fallbackLevel, pos);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks, Level fallbackLevel, double x, double y,
            double z) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks, fallbackLevel, x, y, z);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks, Level fallbackLevel, BlockPos pos) {
        return HbmItemStackUtil.giveOrDropAll(player, stacks, fallbackLevel, pos);
    }

    public static void giveChanceStacksToPlayer(Player player, List<HbmItemStackUtil.ChanceStack> stacks) {
        HbmItemStackUtil.giveChanceStacksToPlayer(player, stacks, player == null ? null : player.getRandom());
    }

    public static void giveChanceStacksToPlayer(Player player, List<HbmItemStackUtil.ChanceStack> stacks,
            RandomSource random) {
        HbmItemStackUtil.giveChanceStacksToPlayer(player, stacks, random);
    }

    public static ItemStack moveMachineStack(List<Slot> slots, StackMover mover, int index, int machineSlotCount,
            int playerInventoryStart, int playerSlotEnd, int... machineInsertionRanges) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < machineSlotCount) {
            if (!mover.move(stack, playerInventoryStart, playerSlotEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveStackToAnyRange(slots, stack, machineInsertionRanges)) {
            return ItemStack.EMPTY;
        }

        finishQuickMove(slot, stack);
        return result;
    }

    public static void finishQuickMove(Slot slot, ItemStack stack) {
        if (slot == null || stack == null) {
            return;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
    }

    public static boolean moveStackToAnyRange(List<Slot> slots, ItemStack stack, int... ranges) {
        if (ranges == null || ranges.length % 2 != 0) {
            return false;
        }
        boolean moved = false;
        for (int i = 0; i < ranges.length; i += 2) {
            moved |= mergeItemStack(slots, stack, ranges[i], ranges[i + 1], false);
            if (stack.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    public static boolean mergeItemStack(List<Slot> slots, ItemStack stack, int start, int end, boolean reverse) {
        if (slots == null || stack == null || stack.isEmpty()) {
            return false;
        }
        int boundedStart = Math.max(0, start);
        int boundedEnd = Math.min(end, slots.size());
        if (boundedStart >= boundedEnd) {
            return false;
        }

        boolean moved = false;
        if (stack.isStackable()) {
            int index = reverse ? boundedEnd - 1 : boundedStart;
            while (!stack.isEmpty() && inRange(index, boundedStart, boundedEnd, reverse)) {
                Slot slot = slots.get(index);
                ItemStack current = slot.getItem();
                if (!current.isEmpty() && HbmItemStackUtil.doesStackDataMatch(stack, current)) {
                    int max = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
                    int transfer = Math.min(stack.getCount(), max - current.getCount());
                    if (transfer > 0 && slot.mayPlace(HbmItemStackUtil.carefulCopyWithSize(stack, transfer))) {
                        stack.shrink(transfer);
                        current.grow(transfer);
                        slot.setByPlayer(current);
                        slot.setChanged();
                        moved = true;
                    }
                }
                index += reverse ? -1 : 1;
            }
        }

        if (!stack.isEmpty()) {
            int index = reverse ? boundedEnd - 1 : boundedStart;
            while (!stack.isEmpty() && inRange(index, boundedStart, boundedEnd, reverse)) {
                Slot slot = slots.get(index);
                ItemStack current = slot.getItem();
                if (current.isEmpty()) {
                    int transfer = Math.min(stack.getCount(),
                            Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize()));
                    if (transfer > 0 && slot.mayPlace(HbmItemStackUtil.carefulCopyWithSize(stack, transfer))) {
                        slot.setByPlayer(stack.split(transfer));
                        slot.setChanged();
                        moved = true;
                    }
                }
                index += reverse ? -1 : 1;
            }
        }
        return moved;
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
        if (object instanceof List<?> list) {
            List<List<ItemStack>> result = new ArrayList<>(list.size());
            for (Object entry : list) {
                appendMixedDisplayEntry(result, entry);
            }
            return List.copyOf(result);
        }
        if (object instanceof Object[] mixed) {
            List<List<ItemStack>> result = new ArrayList<>(mixed.length);
            for (Object entry : mixed) {
                appendMixedDisplayEntry(result, entry);
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

    public static ItemStack[][] extractObject(Object object) {
        return extractDisplayStackMatrix(object);
    }

    private static void appendMixedDisplayEntry(List<List<ItemStack>> result, Object entry) {
        if (entry instanceof ItemStack[] options) {
            result.add(copyDisplayOptions(options));
            return;
        }
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

    private static List<ItemStack> copyDisplayOptions(ItemStack[] options) {
        List<ItemStack> result = new ArrayList<>(options.length);
        for (ItemStack option : options) {
            if (option != null && !option.isEmpty()) {
                result.add(option.copy());
            }
        }
        return List.copyOf(result);
    }

    private static MatchPlan matchIngredients(IItemHandler inventory, int[] slots, List<HbmIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new MatchPlan(List.of());
        }
        if (inventory == null) {
            return null;
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

    private static MatchPlan matchIngredients(Container inventory, int start, int end, List<HbmIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return new MatchPlan(List.of());
        }
        if (inventory == null) {
            return null;
        }
        ItemStack[] copy = new ItemStack[inventory.getContainerSize()];
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            copy[slot] = inventory.getItem(slot).copy();
        }
        return matchIngredients(copy, start, end, ingredients);
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

    private static ItemStack[] copyContainer(Container inventory) {
        ItemStack[] copy = new ItemStack[inventory.getContainerSize()];
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            copy[slot] = inventory.getItem(slot).copy();
        }
        return copy;
    }

    private static ItemStack tryAddItemToContainerCopy(Container inventory, ItemStack[] copy, int start, int end,
            ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (remainder.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, copy.length - 1); slot++) {
            ItemStack current = emptyIfNull(copy[slot]);
            if (!current.isEmpty() && inventory.canPlaceItem(slot, remainder)
                    && HbmItemStackUtil.doesStackDataMatch(current, remainder)) {
                int transfer = Math.min(remainder.getCount(),
                        Math.min(inventory.getMaxStackSize(), current.getMaxStackSize()) - current.getCount());
                if (transfer > 0) {
                    current = current.copy();
                    current.grow(transfer);
                    copy[slot] = current;
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, copy.length - 1); slot++) {
            if (emptyIfNull(copy[slot]).isEmpty() && inventory.canPlaceItem(slot, remainder)) {
                copy[slot] = remainder;
                return ItemStack.EMPTY;
            }
        }
        return remainder;
    }

    private static ItemStack tryAddItemToHandlerCopyUnchecked(ItemStackHandler inventory, ItemStack[] copy, int start,
            int end, ItemStack stack) {
        ItemStack remainder = HbmItemStackUtil.carefulCopy(stack);
        if (remainder.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, copy.length - 1); slot++) {
            ItemStack current = emptyIfNull(copy[slot]);
            if (!current.isEmpty() && HbmItemStackUtil.doesStackDataMatch(current, remainder)) {
                int transfer = Math.min(remainder.getCount(), slotSpace(inventory, slot, current));
                if (transfer > 0) {
                    current = current.copy();
                    current.grow(transfer);
                    copy[slot] = current;
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        for (int slot = Math.max(0, start); slot <= Math.min(end, copy.length - 1); slot++) {
            if (emptyIfNull(copy[slot]).isEmpty()) {
                int transfer = Math.min(remainder.getCount(), slotLimit(inventory, slot, remainder));
                if (transfer > 0) {
                    copy[slot] = HbmItemStackUtil.carefulCopyWithSize(remainder, transfer);
                    remainder.shrink(transfer);
                    if (remainder.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return remainder;
    }

    private static int slotSpace(ItemStackHandler inventory, int slot, ItemStack current) {
        return Math.max(0, slotLimit(inventory, slot, current) - current.getCount());
    }

    private static int slotLimit(ItemStackHandler inventory, int slot, ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), inventory.getSlotLimit(slot));
    }

    private static void removeOneFromHandlerSlot(ItemStackHandler source, int slot, ItemStack stack) {
        ItemStack remaining = stack.copy();
        remaining.shrink(1);
        source.setStackInSlot(slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    }

    private static HbmIngredient withCount(HbmIngredient ingredient, int count) {
        return new HbmIngredient(ingredient.ingredient(), count, ingredient.exactStack(), ingredient.partialNbt(),
                ingredient.legacyId(), ingredient.legacyMeta(), ingredient.legacyWildcard(), ingredient.legacyOreName(),
                ingredient.fluidContainerType(), ingredient.fluidContainerAmount());
    }

    private static int[] range(int size) {
        int[] slots = new int[size];
        Arrays.setAll(slots, index -> index);
        return slots;
    }

    private static int[] rangeInclusive(int start, int end, int size) {
        if (size <= 0 || end < start) {
            return new int[0];
        }
        int from = Math.max(0, start);
        int to = Math.min(size - 1, end);
        if (to < from) {
            return new int[0];
        }
        int[] slots = new int[to - from + 1];
        Arrays.setAll(slots, index -> from + index);
        return slots;
    }

    private static boolean inRange(int index, int start, int end, boolean reverse) {
        return reverse ? index >= start : index < end;
    }

    private static ItemStack emptyIfNull(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private record SlotConsumption(int slot, int count) {
    }

    private record MatchPlan(List<SlotConsumption> consumptions) {
    }

    @FunctionalInterface
    public interface StackMover {
        boolean move(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
    }
}
