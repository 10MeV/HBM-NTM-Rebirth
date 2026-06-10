package com.hbm.ntm.util;

import com.hbm.ntm.recipe.HbmIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

    public static boolean doesArrayHaveSpace(ItemStack[] inventory, int start, int end, ItemStack[] items) {
        return HbmInventoryUtil.doesArrayHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(ItemStack[] inventory, int start, int end, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, ItemStack... items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.size() - 1, items);
    }

    public static boolean doesInventoryHaveSpace(NonNullList<ItemStack> inventory, int start, int end,
            ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, ItemStack... items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0, inventory.getSlots() - 1,
                items);
    }

    public static boolean doesInventoryHaveSpace(IItemHandler inventory, int start, int end, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesInventoryHaveSpace(Container inventory, ItemStack... items) {
        return inventory != null && HbmInventoryUtil.doesInventoryHaveSpace(inventory, 0,
                inventory.getContainerSize() - 1, items);
    }

    public static boolean doesInventoryHaveSpace(Container inventory, int start, int end, ItemStack... items) {
        return HbmInventoryUtil.doesInventoryHaveSpace(inventory, start, end, items);
    }

    public static boolean doesHandlerHaveSpaceUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack... items) {
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(inventory, start, end, items);
    }

    public static ItemStack tryAddItemToHandlerUnchecked(ItemStackHandler inventory, int start, int end,
            ItemStack stack) {
        return HbmInventoryUtil.tryAddItemToHandlerUnchecked(inventory, start, end, stack);
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

    public static boolean doesArrayHaveIngredients(ItemStack[] inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesArrayHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(ItemStack[] inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(IItemHandler inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesInventoryHaveIngredients(Container inventory, int start, int end,
            HbmIngredient... ingredients) {
        return HbmInventoryUtil.doesInventoryHaveIngredients(inventory, start, end, ingredients);
    }

    public static boolean doesPlayerHaveAStacks(Player player, List<HbmIngredient> ingredients,
            boolean shouldRemove) {
        return HbmInventoryUtil.doesPlayerHaveAStacks(player, ingredients, shouldRemove);
    }

    public static int countAStackMatches(Player player, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(player, ingredient, ignoreSize);
    }

    public static int countAStackMatches(ItemStack[] inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(Container inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, ingredient, ignoreSize);
    }

    public static int countAStackMatches(IItemHandler inventory, HbmIngredient ingredient, boolean ignoreSize) {
        return HbmInventoryUtil.countAStackMatches(inventory, ingredient, ignoreSize);
    }

    public static boolean doesPlayerHaveAStack(Player player, HbmIngredient ingredient, boolean shouldRemove,
            boolean ignoreSize) {
        return HbmInventoryUtil.doesPlayerHaveAStack(player, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(ItemStack[] inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(Container inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static boolean doesInventoryHaveAStack(IItemHandler inventory, HbmIngredient ingredient,
            boolean shouldRemove, boolean ignoreSize) {
        return HbmInventoryUtil.doesInventoryHaveAStack(inventory, ingredient, shouldRemove, ignoreSize);
    }

    public static int countTagMatches(Player player, TagKey<Item> tag) {
        return HbmInventoryUtil.countTagMatches(player, tag);
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

    public static void giveChanceStacksToPlayer(Player player, List<HbmItemStackUtil.ChanceStack> stacks) {
        HbmInventoryUtil.giveChanceStacksToPlayer(player, stacks);
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
