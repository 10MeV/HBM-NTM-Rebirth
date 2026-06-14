package com.hbm.util;

import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmItemStackUtil;

import com.hbm.ntm.recipe.HbmIngredient;
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
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

/**
 * Legacy-name inventory helper facade for migrated code in the modern package.
 */
@Deprecated(forRemoval = false)
public final class InventoryUtil {
    private InventoryUtil() {
    }

    public static boolean doesStackDataMatch(ItemStack first, ItemStack second) {
        return HbmInventoryUtil.doesStackDataMatch(first, second);
    }

    public static ItemStack tryAddItemToInventory(ItemStack[] inventory, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, stack);
    }

    public static ItemStack tryAddItemToInventory(ItemStack[] inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToExistingStack(ItemStack[] inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToExistingStack(inventory, start, end, stack);
    }

    public static boolean tryAddItemToNewSlot(ItemStack[] inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToNewSlot(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToInventory(NonNullList<ItemStack> inventory, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, stack);
    }

    public static ItemStack tryAddItemToInventory(NonNullList<ItemStack> inventory, int start, int end,
            ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToExistingStack(NonNullList<ItemStack> inventory, int start, int end,
            ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToExistingStack(inventory, start, end, stack);
    }

    public static boolean tryAddItemToNewSlot(NonNullList<ItemStack> inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToNewSlot(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToInventory(Player player, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(player, stack);
    }

    public static ItemStack tryAddItemToInventory(Inventory inventory, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, stack);
    }

    public static ItemStack tryAddItemToInventory(Inventory inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToExistingStack(Inventory inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToExistingStack(inventory, start, end, stack);
    }

    public static boolean tryAddItemToNewSlot(Inventory inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToNewSlot(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToInventory(IItemHandler inventory, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, stack);
    }

    public static ItemStack tryAddItemToInventory(IItemHandler inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToExistingStack(IItemHandler inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToExistingStack(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToNewSlot(IItemHandler inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToNewSlot(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToInventory(Container inventory, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, stack);
    }

    public static ItemStack tryAddItemToInventory(Container inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToInventory(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToExistingStack(Container inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToExistingStack(inventory, start, end, stack);
    }

    public static boolean tryAddItemToNewSlot(Container inventory, int start, int end, ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToNewSlot(inventory, start, end, stack);
    }

    public static boolean doesInventoryHaveSpace(ItemStack[] inventory, ItemStack... items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.length - 1, items);
    }

    public static boolean doesInventoryHaveSpace(ItemStack[] inventory, List<ItemStack> items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.length - 1,
                items == null ? new ItemStack[0] : items.toArray(ItemStack[]::new));
    }

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, ItemStack... items) {
        return HbmInventoryUtil.doesArrayHaveSpace(inventory, items);
    }

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, List<ItemStack> items) {
        return HbmInventoryUtil.doesArrayHaveSpace(inventory, items);
    }

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, int start, int end, ItemStack[] items) {
        return HbmInventoryUtil.doesArrayHaveSpace(inventory, start, end, items);
    }

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, int start, int end, List<ItemStack> items) {
        return HbmInventoryUtil.doesArrayHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(ItemStack[] inventory, int start, int end, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, ItemStack... items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.size() - 1, items);
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, List<ItemStack> items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.size() - 1,
                items);
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, int start, int end,
            ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, int start, int end,
            List<ItemStack> items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(Player player, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(player, items);
    }

    public static boolean doesInventoryHaveSpace(Player player, List<ItemStack> items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(player, items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, List<ItemStack> items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, int start, int end, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(Inventory inventory, int start, int end, List<ItemStack> items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesPlayerInventoryHaveSpace(Player player, ItemStack... items) {
        return HbmInventoryUtil.doesPlayerInventoryHaveSpace(player, items);
    }

    public static boolean doesPlayerInventoryHaveSpace(Player player, List<ItemStack> items) {
        return HbmInventoryUtil.doesPlayerInventoryHaveSpace(player, items);
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, ItemStack... items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.getSlots() - 1,
                items);
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, List<ItemStack> items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.getSlots() - 1,
                items);
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, int start, int end, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, int start, int end, List<ItemStack> items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(Container inventory, ItemStack... items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0,
                inventory.getContainerSize() - 1, items);
    }

    public static boolean doesInventoryHaveSpace(Container inventory, List<ItemStack> items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0,
                inventory.getContainerSize() - 1, items);
    }

    public static boolean doesInventoryHaveSpace(Container inventory, int start, int end, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(Container inventory, int start, int end, List<ItemStack> items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesHandlerHaveSpaceUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack... items) {
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(inventory, start, end, items);
    }

    public static boolean doesHandlerHaveSpaceUnchecked(ItemStackHandler inventory, int start, int end,
            List<ItemStack> items) {
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(inventory, start, end, items);
    }

    public static ItemStack tryAddItemToHandlerUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToHandlerUnchecked(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToExistingStacksUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToExistingStacksUnchecked(inventory, start, end, stack);
    }

    public static ItemStack tryAddItemToFirstNewSlotUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToFirstNewSlotUnchecked(inventory, start, end, stack);
    }

    public static boolean moveSingleItemFromHandlerToHandler(ItemStackHandler source, int sourceStart, int sourceEnd,
            IItemHandler target) {
        return HbmInventoryUtil.moveSingleItemFromHandlerToHandler(source, sourceStart, sourceEnd, target);
    }

    public static boolean moveSingleItemFromHandlerToContainer(ItemStackHandler source, int sourceStart, int sourceEnd,
            Container target) {
        return HbmInventoryUtil.moveSingleItemFromHandlerToContainer(source, sourceStart, sourceEnd, target);
    }

    public static boolean tryConsumeAStack(ItemStack[] inventory, int start, int end, HbmIngredient ingredient) {
        return HbmInventoryUtil.tryConsumeAStack(inventory, start, end, ingredient);
    }

    public static boolean tryConsumeAStack(IItemHandler inventory, int start, int end, HbmIngredient ingredient) {
        return HbmInventoryUtil.tryConsumeAStack(inventory, start, end, ingredient);
    }

    public static boolean tryConsumeAStack(Container inventory, int start, int end, HbmIngredient ingredient) {
        return HbmInventoryUtil.tryConsumeAStack(inventory, start, end, ingredient);
    }

    public static boolean tryConsumeAStack(Inventory inventory, HbmIngredient ingredient) {
        return HbmInventoryUtil.tryConsumeAStack(inventory, ingredient);
    }

    public static boolean tryConsumeAStack(Inventory inventory, int start, int end, HbmIngredient ingredient) {
        return HbmInventoryUtil.tryConsumeAStack(inventory, start, end, ingredient);
    }

    public static boolean tryConsumeIngredients(ItemStack[] inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, start, end, simulate, ingredients);
    }

    public static boolean tryConsumeIngredients(ItemStackHandler inventory, int[] slots,
            List<HbmIngredient> ingredients, boolean simulate) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, slots, ingredients, simulate);
    }

    public static boolean tryConsumeIngredients(IItemHandler inventory, int[] slots,
            List<HbmIngredient> ingredients, boolean simulate) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, slots, ingredients, simulate);
    }

    public static boolean tryConsumeIngredients(IItemHandler inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, start, end, simulate, ingredients);
    }

    public static boolean tryConsumeIngredients(Container inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, start, end, simulate, ingredients);
    }

    public static boolean tryConsumeIngredients(Inventory inventory, List<HbmIngredient> ingredients,
            boolean simulate) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, ingredients, simulate);
    }

    public static boolean tryConsumeIngredients(Inventory inventory, int start, int end, boolean simulate,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, start, end, simulate, ingredients);
    }

    public static boolean tryConsumeIngredients(Inventory inventory, int start, int end,
            List<HbmIngredient> ingredients, boolean simulate) {
        return HbmInventoryUtil.tryConsumeIngredients(inventory, start, end, ingredients, simulate);
    }

    public static boolean doesArrayHaveIngredients(ItemStack[] inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesArrayHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesArrayHaveIngredients(ItemStack[] inventory, HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesArrayHaveIngredients(inventory, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(ItemStack[] inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(ItemStackHandler inventory, int[] slots,
            List<HbmIngredient> ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, slots, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(IItemHandler inventory, int[] slots,
            List<HbmIngredient> ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, slots, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(IItemHandler inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(Container inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(Inventory inventory, List<HbmIngredient> ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(Inventory inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesPlayerHaveIngredients(Player player, List<HbmIngredient> ingredients,
            boolean shouldRemove) {
        return HbmInventoryUtil.doesPlayerHaveIngredients(player, ingredients, shouldRemove);
    }

    public static boolean doesPlayerHaveAStacks(Player player, List<HbmIngredient> ingredients,
            boolean shouldRemove) {
        return HbmInventoryUtil.doesPlayerHaveAStacks(player, ingredients, shouldRemove);
    }

    public static int countIngredientMatches(Player player, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(player, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Player player, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(player, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Inventory inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Inventory inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Inventory inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Inventory inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(NonNullList<ItemStack> inventory, int start, int end,
            HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countAStackMatches(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(NonNullList<ItemStack> inventory, int start, int end,
            HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(ItemStack[] inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(ItemStack[] inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(ItemStack[] inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countAStackMatches(ItemStack[] inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Container inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Container inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(Container inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Container inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(IItemHandler inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(IItemHandler inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, ingredient, ignoreSize);
    }

    public static int countIngredientMatches(IItemHandler inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countIngredientMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static int countAStackMatches(IItemHandler inventory, int start, int end, HbmIngredient ingredient,
            boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, start, end, ingredient, ignoreSize);
    }

    public static boolean doesPlayerHaveIngredient(Player player, HbmIngredient ingredient, boolean shouldRemove,
            boolean ignoreSize) {
        return HbmInventoryUtil.doesPlayerHaveIngredient(player, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesPlayerHaveAStack(Player player, HbmIngredient ingredient, boolean shouldRemove,
            boolean ignoreSize) {
        return HbmInventoryUtil.doesPlayerHaveAStack(player, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Inventory inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Inventory inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove,
                ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(Inventory inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(Inventory inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(ItemStack[] inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(ItemStack[] inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(ItemStack[] inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove,
                ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(ItemStack[] inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(NonNullList<ItemStack> inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(NonNullList<ItemStack> inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove,
                ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(NonNullList<ItemStack> inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Container inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(Container inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(Container inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove,
                ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(Container inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(IItemHandler inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(IItemHandler inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveIngredient(IItemHandler inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveIngredient(inventory, start, end, ingredient, shouldRemove,
                ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(IItemHandler inventory, int start, int end,
            HbmIngredient ingredient, boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, start, end, ingredient, shouldRemove, ignoreSize);
    }

    public static int countTagMatches(Player player, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(player, tag);
    }

    public static int countTagMatches(Inventory inventory, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, tag);
    }

    public static int countTagMatches(Inventory inventory, int start, int end, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, start, end, tag);
    }

    public static int countTagMatches(ItemStack[] inventory, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, tag);
    }

    public static int countTagMatches(ItemStack[] inventory, int start, int end, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, start, end, tag);
    }

    public static int countTagMatches(NonNullList<ItemStack> inventory, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, tag);
    }

    public static int countTagMatches(NonNullList<ItemStack> inventory, int start, int end, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, start, end, tag);
    }

    public static int countTagMatches(IItemHandler inventory, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, tag);
    }

    public static int countTagMatches(IItemHandler inventory, int start, int end, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, start, end, tag);
    }

    public static int countTagMatches(Container inventory, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, tag);
    }

    public static int countTagMatches(Container inventory, int start, int end, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(inventory, start, end, tag);
    }

    public static int countLegacyOreMatches(Player player, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(player, legacyOreName);
    }

    public static int countOreDictMatches(Player player, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(player, legacyOreName);
    }

    public static int countLegacyOreMatches(Inventory inventory, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countOreDictMatches(Inventory inventory, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(Inventory inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countOreDictMatches(Inventory inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(ItemStack[] inventory, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countOreDictMatches(ItemStack[] inventory, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(ItemStack[] inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countOreDictMatches(ItemStack[] inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(NonNullList<ItemStack> inventory, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countOreDictMatches(NonNullList<ItemStack> inventory, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countOreDictMatches(NonNullList<ItemStack> inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(IItemHandler inventory, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countOreDictMatches(IItemHandler inventory, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(IItemHandler inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countOreDictMatches(IItemHandler inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, start, end, legacyOreName);
    }

    public static int countLegacyOreMatches(Container inventory, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, legacyOreName);
    }

    public static int countOreDictMatches(Container inventory, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, legacyOreName);
    }

    public static int countLegacyOreMatches(Container inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countLegacyOreMatches(inventory, start, end, legacyOreName);
    }

    public static int countOreDictMatches(Container inventory, int start, int end, String legacyOreName) {
        return HbmInventoryUtil.countOreDictMatches(inventory, start, end, legacyOreName);
    }

    public static boolean hasTagMatches(Player player, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(player, tag, count);
    }

    public static boolean hasTagMatches(Inventory inventory, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, tag, count);
    }

    public static boolean hasTagMatches(Inventory inventory, int start, int end, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, start, end, tag, count);
    }

    public static boolean hasTagMatches(ItemStack[] inventory, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, tag, count);
    }

    public static boolean hasTagMatches(ItemStack[] inventory, int start, int end, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, start, end, tag, count);
    }

    public static boolean hasTagMatches(NonNullList<ItemStack> inventory, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, tag, count);
    }

    public static boolean hasTagMatches(NonNullList<ItemStack> inventory, int start, int end, TagKey<Item> tag,
            int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, start, end, tag, count);
    }

    public static boolean hasTagMatches(IItemHandler inventory, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, tag, count);
    }

    public static boolean hasTagMatches(IItemHandler inventory, int start, int end, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, start, end, tag, count);
    }

    public static boolean hasTagMatches(Container inventory, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, tag, count);
    }

    public static boolean hasTagMatches(Container inventory, int start, int end, TagKey<Item> tag, int count) {
        return HbmInventoryUtil.hasTagMatches(inventory, start, end, tag, count);
    }

    public static boolean hasLegacyOreMatches(Player player, String legacyOreName, int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(player, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(Player player, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(player, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Inventory inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(Inventory inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Inventory inventory, int start, int end, String legacyOreName,
            int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(Inventory inventory, int start, int end, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(ItemStack[] inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(ItemStack[] inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName, int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(NonNullList<ItemStack> inventory, int start, int end, String legacyOreName,
            int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(IItemHandler inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(IItemHandler inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Container inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(Container inventory, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, legacyOreName, count);
    }

    public static boolean hasLegacyOreMatches(Container inventory, int start, int end, String legacyOreName,
            int count) {
        return HbmInventoryUtil.hasLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static boolean hasOreDictMatches(Container inventory, int start, int end, String legacyOreName, int count) {
        return HbmInventoryUtil.hasOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeTagMatches(Player player, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(player, tag, count);
    }

    public static void consumeTagMatches(Inventory inventory, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, tag, count);
    }

    public static void consumeTagMatches(Inventory inventory, int start, int end, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, start, end, tag, count);
    }

    public static void consumeTagMatches(ItemStack[] inventory, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, tag, count);
    }

    public static void consumeTagMatches(ItemStack[] inventory, int start, int end, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, start, end, tag, count);
    }

    public static void consumeTagMatches(NonNullList<ItemStack> inventory, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, tag, count);
    }

    public static void consumeTagMatches(NonNullList<ItemStack> inventory, int start, int end, TagKey<Item> tag,
            int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, start, end, tag, count);
    }

    public static void consumeTagMatches(IItemHandler inventory, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, tag, count);
    }

    public static void consumeTagMatches(IItemHandler inventory, int start, int end, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, start, end, tag, count);
    }

    public static void consumeTagMatches(Container inventory, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, tag, count);
    }

    public static void consumeTagMatches(Container inventory, int start, int end, TagKey<Item> tag, int count) {
        HbmInventoryUtil.consumeTagMatches(inventory, start, end, tag, count);
    }

    public static void consumeLegacyOreMatches(Player player, String legacyOreName, int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(player, legacyOreName, count);
    }

    public static void consumeOreDictMatches(Player player, String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(player, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Inventory inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeOreDictMatches(Inventory inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Inventory inventory, int start, int end, String legacyOreName,
            int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeOreDictMatches(Inventory inventory, int start, int end, String legacyOreName,
            int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(ItemStack[] inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeOreDictMatches(ItemStack[] inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeOreDictMatches(ItemStack[] inventory, int start, int end, String legacyOreName,
            int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeOreDictMatches(NonNullList<ItemStack> inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName, int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeOreDictMatches(NonNullList<ItemStack> inventory, int start, int end,
            String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(IItemHandler inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeOreDictMatches(IItemHandler inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeOreDictMatches(IItemHandler inventory, int start, int end, String legacyOreName,
            int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Container inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, legacyOreName, count);
    }

    public static void consumeOreDictMatches(Container inventory, String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, legacyOreName, count);
    }

    public static void consumeLegacyOreMatches(Container inventory, int start, int end, String legacyOreName,
            int count) {
        HbmInventoryUtil.consumeLegacyOreMatches(inventory, start, end, legacyOreName, count);
    }

    public static void consumeOreDictMatches(Container inventory, int start, int end, String legacyOreName, int count) {
        HbmInventoryUtil.consumeOreDictMatches(inventory, start, end, legacyOreName, count);
    }

    public static List<ItemStack> clearToDrops(NonNullList<ItemStack> items) {
        return HbmInventoryUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(ItemStack[] items) {
        return HbmInventoryUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(Container items) {
        return HbmInventoryUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(IItemHandler items) {
        return HbmInventoryUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(ItemStackHandler items) {
        return HbmInventoryUtil.clearToDrops(items);
    }

    public static void spillItems(Level level, BlockPos pos, IItemHandler items, RandomSource random) {
        HbmInventoryUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, IItemHandler items) {
        HbmInventoryUtil.spillItems(level, pos, items);
    }

    public static void spillItems(Level level, BlockPos pos, NonNullList<ItemStack> items, RandomSource random) {
        HbmInventoryUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, NonNullList<ItemStack> items) {
        HbmInventoryUtil.spillItems(level, pos, items);
    }

    public static void spillItems(Level level, BlockPos pos, ItemStack[] items, RandomSource random) {
        HbmInventoryUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, ItemStack[] items) {
        HbmInventoryUtil.spillItems(level, pos, items);
    }

    public static void spillItems(Level level, BlockPos pos, Container items, RandomSource random) {
        HbmInventoryUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, Container items) {
        HbmInventoryUtil.spillItems(level, pos, items);
    }

    public static void spillStack(Level level, BlockPos pos, ItemStack stack, RandomSource random) {
        HbmInventoryUtil.spillStack(level, pos, stack, random);
    }

    public static void spillStack(Level level, BlockPos pos, ItemStack stack) {
        HbmInventoryUtil.spillStack(level, pos, stack);
    }

    public static void dropStack(Level level, double x, double y, double z, ItemStack stack) {
        HbmInventoryUtil.dropStack(level, x, y, z, stack);
    }

    public static void dropStack(Level level, BlockPos pos, ItemStack stack) {
        HbmInventoryUtil.dropStack(level, pos, stack);
    }

    public static void dropStacks(Level level, double x, double y, double z, Iterable<ItemStack> stacks) {
        HbmInventoryUtil.dropStacks(level, x, y, z, stacks);
    }

    public static void dropStacks(Level level, BlockPos pos, Iterable<ItemStack> stacks) {
        HbmInventoryUtil.dropStacks(level, pos, stacks);
    }

    public static void dropStacks(Level level, double x, double y, double z, ItemStack[] stacks) {
        HbmInventoryUtil.dropStacks(level, x, y, z, stacks);
    }

    public static void dropStacks(Level level, BlockPos pos, ItemStack[] stacks) {
        HbmInventoryUtil.dropStacks(level, pos, stacks);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack) {
        return HbmInventoryUtil.giveOrDrop(player, stack);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, boolean throwRandomly) {
        return HbmInventoryUtil.giveOrDrop(player, stack, throwRandomly);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, Level fallbackLevel, double x, double y,
            double z) {
        return HbmInventoryUtil.giveOrDrop(player, stack, fallbackLevel, x, y, z);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, Level fallbackLevel, BlockPos pos) {
        return HbmInventoryUtil.giveOrDrop(player, stack, fallbackLevel, pos);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks, boolean throwRandomly) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks, throwRandomly);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks, boolean throwRandomly) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks, throwRandomly);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks, Level fallbackLevel, double x,
            double y, double z) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks, fallbackLevel, x, y, z);
    }

    public static boolean giveOrDropAll(Player player, Iterable<ItemStack> stacks, Level fallbackLevel, BlockPos pos) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks, fallbackLevel, pos);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks, Level fallbackLevel, double x, double y,
            double z) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks, fallbackLevel, x, y, z);
    }

    public static boolean giveOrDropAll(Player player, ItemStack[] stacks, Level fallbackLevel, BlockPos pos) {
        return HbmInventoryUtil.giveOrDropAll(player, stacks, fallbackLevel, pos);
    }

    public static void giveChanceStacksToPlayer(Player player, List<HbmItemStackUtil.ChanceStack> stacks) {
        HbmInventoryUtil.giveChanceStacksToPlayer(player, stacks);
    }

    public static void giveChanceStacksToPlayer(Player player, List<HbmItemStackUtil.ChanceStack> stacks,
            RandomSource random) {
        HbmInventoryUtil.giveChanceStacksToPlayer(player, stacks, random);
    }

    public static ItemStack moveMachineStack(List<Slot> slots, HbmInventoryUtil.StackMover mover, int index,
            int machineSlotCount, int playerInventoryStart, int playerSlotEnd, int... machineInsertionRanges) {
        return HbmInventoryUtil.moveMachineStack(slots, mover, index, machineSlotCount, playerInventoryStart,
                playerSlotEnd, machineInsertionRanges);
    }

    public static void finishQuickMove(Slot slot, ItemStack stack) {
        HbmInventoryUtil.finishQuickMove(slot, stack);
    }

    public static boolean moveStackToAnyRange(List<Slot> slots, ItemStack stack, int... ranges) {
        return HbmInventoryUtil.moveStackToAnyRange(slots, stack, ranges);
    }

    public static boolean mergeItemStack(List<Slot> slots, ItemStack stack, int start, int end, boolean reverse) {
        return HbmInventoryUtil.mergeItemStack(slots, stack, start, end, reverse);
    }

    public static List<List<ItemStack>> extractDisplayStacks(Object object) {
        return HbmInventoryUtil.extractDisplayStacks(object);
    }

    public static ItemStack[][] extractDisplayStackMatrix(Object object) {
        return HbmInventoryUtil.extractDisplayStackMatrix(object);
    }

    public static ItemStack[][] extractObject(Object object) {
        return HbmInventoryUtil.extractObject(object);
    }
}
