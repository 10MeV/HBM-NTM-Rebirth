package com.hbm.ntm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collection;
import java.util.List;

/**
 * Legacy-name ItemStack helper facade.
 */
@Deprecated(forRemoval = false)
public final class ItemStackUtil {
    public static final String LEGACY_ITEMS_TAG = HbmItemStackUtil.LEGACY_ITEMS_TAG;
    public static final String LEGACY_SLOT_TAG = HbmItemStackUtil.LEGACY_SLOT_TAG;

    private ItemStackUtil() {
    }

    public static ItemStack carefulCopy(ItemStack stack) {
        return HbmItemStackUtil.carefulCopy(stack);
    }

    public static ItemStack carefulCopyWithSize(ItemStack stack, int size) {
        return HbmItemStackUtil.carefulCopyWithSize(stack, size);
    }

    public static ItemStack[] carefulCopyArray(ItemStack[] array) {
        return HbmItemStackUtil.carefulCopyArray(array);
    }

    public static ItemStack[] carefulCopyArray(ItemStack[] array, int start, int end) {
        return HbmItemStackUtil.carefulCopyArray(array, start, end);
    }

    public static ItemStack[] carefulCopyArrayTruncate(ItemStack[] array, int start, int end) {
        return HbmItemStackUtil.carefulCopyArrayTruncate(array, start, end);
    }

    public static NonNullList<ItemStack> carefulCopyList(NonNullList<ItemStack> stacks) {
        return HbmItemStackUtil.carefulCopyList(stacks);
    }

    public static ItemStack[] carefulCopyArray(IItemHandler items) {
        return HbmItemStackUtil.carefulCopyArray(items);
    }

    public static ItemStack addTooltipToStack(ItemStack stack, String... lines) {
        return HbmItemStackUtil.addTooltipToStack(stack, lines);
    }

    public static ItemStack addStackSizeLabel(ItemStack stack) {
        return HbmItemStackUtil.addStackSizeLabel(stack);
    }

    public static void addStacksToNBT(ItemStack stack, ItemStack... stacks) {
        HbmItemStackUtil.addStacksToNBT(stack, stacks);
    }

    public static void addStacksToNbt(ItemStack stack, ItemStack... stacks) {
        HbmItemStackUtil.addStacksToNbt(stack, stacks);
    }

    public static void addStacksToNbt(ItemStack stack, List<ItemStack> stacks) {
        HbmItemStackUtil.addStacksToNbt(stack, stacks);
    }

    public static void setStacksToNbt(ItemStack stack, ItemStack[] stacks, boolean removeWhenEmpty) {
        HbmItemStackUtil.setStacksToNbt(stack, stacks, removeWhenEmpty);
    }

    public static void setStacksToNbt(ItemStack stack, NonNullList<ItemStack> stacks, boolean removeWhenEmpty) {
        HbmItemStackUtil.setStacksToNbt(stack, stacks, removeWhenEmpty);
    }

    public static ItemStack[] readStacksFromNBT(ItemStack stack) {
        return HbmItemStackUtil.readStacksFromNBT(stack);
    }

    public static ItemStack[] readStacksFromNBT(ItemStack stack, int count) {
        return HbmItemStackUtil.readStacksFromNBT(stack, count);
    }

    public static ItemStack[] readStackArrayFromNbt(ItemStack stack) {
        return HbmItemStackUtil.readStackArrayFromNbt(stack);
    }

    public static ItemStack[] readStackArrayFromNbt(ItemStack stack, int count) {
        return HbmItemStackUtil.readStackArrayFromNbt(stack, count);
    }

    public static NonNullList<ItemStack> readStacksFromNbt(ItemStack stack) {
        return HbmItemStackUtil.readStacksFromNbt(stack);
    }

    public static NonNullList<ItemStack> readStacksFromNbt(ItemStack stack, int count) {
        return HbmItemStackUtil.readStacksFromNbt(stack, count);
    }

    public static boolean hasLegacyItemsTag(ItemStack stack) {
        return HbmItemStackUtil.hasLegacyItemsTag(stack);
    }

    public static List<String> getOreDictNames(ItemStack stack) {
        return HbmItemStackUtil.getOreDictNames(stack);
    }

    public static List<String> getTagNames(ItemStack stack) {
        return HbmItemStackUtil.getTagNames(stack);
    }

    public static List<ResourceLocation> getTagIds(ItemStack stack) {
        return HbmItemStackUtil.getTagIds(stack);
    }

    public static String getModIdFromItemStack(ItemStack stack) {
        return HbmItemStackUtil.getModIdFromItemStack(stack);
    }

    public static boolean areStacksCompatible(ItemStack first, ItemStack second) {
        return HbmItemStackUtil.areStacksCompatible(first, second);
    }

    public static boolean doesStackDataMatch(ItemStack first, ItemStack second) {
        return HbmItemStackUtil.doesStackDataMatch(first, second);
    }

    public static boolean isInAnyTag(ItemStack stack, Collection<TagKey<Item>> tags) {
        return HbmItemStackUtil.isInAnyTag(stack, tags);
    }

    public static void addNBTFromString(ItemStack stack, String nbt) {
        HbmItemStackUtil.addNBTFromString(stack, nbt);
    }

    public static void addNbtFromString(ItemStack stack, String nbt) {
        HbmItemStackUtil.addNbtFromString(stack, nbt);
    }

    public static CompoundTag saveLegacyItems(ItemStack[] items) {
        return HbmItemStackUtil.saveLegacyItems(items);
    }

    public static CompoundTag saveLegacyItems(NonNullList<ItemStack> items) {
        return HbmItemStackUtil.saveLegacyItems(items);
    }

    public static CompoundTag saveLegacyItems(IItemHandler items) {
        return HbmItemStackUtil.saveLegacyItems(items);
    }

    public static CompoundTag saveLegacyItems(ItemStackHandler items) {
        return HbmItemStackUtil.saveLegacyItems(items);
    }

    public static CompoundTag saveLegacyItems(Container items) {
        return HbmItemStackUtil.saveLegacyItems(items);
    }

    public static ListTag saveSlottedItems(ItemStack[] items, String slotKey) {
        return HbmItemStackUtil.saveSlottedItems(items, slotKey);
    }

    public static ListTag saveSlottedItems(NonNullList<ItemStack> items, String slotKey) {
        return HbmItemStackUtil.saveSlottedItems(items, slotKey);
    }

    public static ListTag saveSlottedItems(IItemHandler items, String slotKey) {
        return HbmItemStackUtil.saveSlottedItems(items, slotKey);
    }

    public static ListTag saveSlottedItems(ItemStackHandler items, String slotKey) {
        return HbmItemStackUtil.saveSlottedItems(items, slotKey);
    }

    public static ListTag saveSlottedItems(Container items, String slotKey) {
        return HbmItemStackUtil.saveSlottedItems(items, slotKey);
    }

    public static void saveSlottedItemsToTag(CompoundTag target, String listKey, String slotKey,
            ItemStack[] items) {
        HbmItemStackUtil.saveSlottedItemsToTag(target, listKey, slotKey, items);
    }

    public static void saveSlottedItemsToTag(CompoundTag target, String listKey, String slotKey,
            NonNullList<ItemStack> items) {
        HbmItemStackUtil.saveSlottedItemsToTag(target, listKey, slotKey, items);
    }

    public static void saveSlottedItemsToTag(CompoundTag target, String listKey, String slotKey,
            IItemHandler items) {
        HbmItemStackUtil.saveSlottedItemsToTag(target, listKey, slotKey, items);
    }

    public static void saveSlottedItemsToTag(CompoundTag target, String listKey, String slotKey,
            ItemStackHandler items) {
        HbmItemStackUtil.saveSlottedItemsToTag(target, listKey, slotKey, items);
    }

    public static void saveSlottedItemsToTag(CompoundTag target, String listKey, String slotKey, Container items) {
        HbmItemStackUtil.saveSlottedItemsToTag(target, listKey, slotKey, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, ItemStack[] items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, String key, ItemStack[] items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, key, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, NonNullList<ItemStack> items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, String key, NonNullList<ItemStack> items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, key, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, IItemHandler items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, String key, IItemHandler items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, key, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, ItemStackHandler items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, Container items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, items);
    }

    public static void saveLegacyItemsToTag(CompoundTag target, String key, Container items) {
        HbmItemStackUtil.saveLegacyItemsToTag(target, key, items);
    }

    public static void saveLegacyItemsCompoundToTag(CompoundTag target, String key, ItemStack[] items) {
        HbmItemStackUtil.saveLegacyItemsCompoundToTag(target, key, items);
    }

    public static void saveLegacyItemsCompoundToTag(CompoundTag target, String key, NonNullList<ItemStack> items) {
        HbmItemStackUtil.saveLegacyItemsCompoundToTag(target, key, items);
    }

    public static void saveLegacyItemsCompoundToTag(CompoundTag target, String key, IItemHandler items) {
        HbmItemStackUtil.saveLegacyItemsCompoundToTag(target, key, items);
    }

    public static void saveLegacyItemsCompoundToTag(CompoundTag target, String key, ItemStackHandler items) {
        HbmItemStackUtil.saveLegacyItemsCompoundToTag(target, key, items);
    }

    public static void saveLegacyItemsCompoundToTag(CompoundTag target, String key, Container items) {
        HbmItemStackUtil.saveLegacyItemsCompoundToTag(target, key, items);
    }

    public static void loadLegacyItems(CompoundTag tag, ItemStack[] items) {
        HbmItemStackUtil.loadLegacyItems(tag, items);
    }

    public static void loadLegacyItems(CompoundTag tag, String key, ItemStack[] items) {
        HbmItemStackUtil.loadLegacyItems(tag, key, items);
    }

    public static void loadLegacyItemsCompound(CompoundTag tag, String key, ItemStack[] items) {
        HbmItemStackUtil.loadLegacyItemsCompound(tag, key, items);
    }

    public static void loadLegacyItems(CompoundTag tag, NonNullList<ItemStack> items) {
        HbmItemStackUtil.loadLegacyItems(tag, items);
    }

    public static void loadLegacyItems(CompoundTag tag, String key, NonNullList<ItemStack> items) {
        HbmItemStackUtil.loadLegacyItems(tag, key, items);
    }

    public static void loadLegacyItemsCompound(CompoundTag tag, String key, NonNullList<ItemStack> items) {
        HbmItemStackUtil.loadLegacyItemsCompound(tag, key, items);
    }

    public static void loadLegacyItems(CompoundTag tag, ItemStackHandler items) {
        HbmItemStackUtil.loadLegacyItems(tag, items);
    }

    public static void loadLegacyItems(CompoundTag tag, String key, ItemStackHandler items) {
        HbmItemStackUtil.loadLegacyItems(tag, key, items);
    }

    public static void loadLegacyItemsCompound(CompoundTag tag, String key, ItemStackHandler items) {
        HbmItemStackUtil.loadLegacyItemsCompound(tag, key, items);
    }

    public static void loadLegacyItems(CompoundTag tag, Container items) {
        HbmItemStackUtil.loadLegacyItems(tag, items);
    }

    public static void loadLegacyItems(CompoundTag tag, String key, Container items) {
        HbmItemStackUtil.loadLegacyItems(tag, key, items);
    }

    public static void loadLegacyItemsCompound(CompoundTag tag, String key, Container items) {
        HbmItemStackUtil.loadLegacyItemsCompound(tag, key, items);
    }

    public static NonNullList<ItemStack> loadLegacyItems(CompoundTag tag, int slotCount) {
        return HbmItemStackUtil.loadLegacyItems(tag, slotCount);
    }

    public static NonNullList<ItemStack> loadLegacyOrForgeItems(CompoundTag tag, int slotCount) {
        return HbmItemStackUtil.loadLegacyOrForgeItems(tag, slotCount);
    }

    public static void loadLegacyOrForgeItems(CompoundTag tag, ItemStack[] items) {
        HbmItemStackUtil.loadLegacyOrForgeItems(tag, items);
    }

    public static void loadLegacyOrForgeItems(CompoundTag tag, NonNullList<ItemStack> items) {
        HbmItemStackUtil.loadLegacyOrForgeItems(tag, items);
    }

    public static void loadLegacyOrForgeItems(CompoundTag tag, ItemStackHandler items) {
        HbmItemStackUtil.loadLegacyOrForgeItems(tag, items);
    }

    public static void loadLegacyOrForgeItems(CompoundTag tag, Container items) {
        HbmItemStackUtil.loadLegacyOrForgeItems(tag, items);
    }

    public static void loadLegacyOrForgeItemsCompound(CompoundTag tag, String key, ItemStack[] items) {
        HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, key, items);
    }

    public static void loadLegacyOrForgeItemsCompound(CompoundTag tag, String key, NonNullList<ItemStack> items) {
        HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, key, items);
    }

    public static void loadLegacyOrForgeItemsCompound(CompoundTag tag, String key, ItemStackHandler items) {
        HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, key, items);
    }

    public static void loadLegacyOrForgeItemsCompound(CompoundTag tag, String key, Container items) {
        HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, key, items);
    }

    public static void loadSlottedItems(CompoundTag tag, String listKey, String slotKey, ItemStack[] target) {
        HbmItemStackUtil.loadSlottedItems(tag, listKey, slotKey, target);
    }

    public static void loadSlottedItems(CompoundTag tag, String listKey, String slotKey, ItemStackHandler target) {
        HbmItemStackUtil.loadSlottedItems(tag, listKey, slotKey, target);
    }

    public static void loadSlottedItems(CompoundTag tag, String listKey, String slotKey, Container target) {
        HbmItemStackUtil.loadSlottedItems(tag, listKey, slotKey, target);
    }

    public static void loadSlottedItems(CompoundTag tag, String listKey, String slotKey,
            NonNullList<ItemStack> target) {
        HbmItemStackUtil.loadSlottedItems(tag, listKey, slotKey, target);
    }

    public static void loadSlottedItems(ListTag list, String slotKey, ItemStack[] target) {
        HbmItemStackUtil.loadSlottedItems(list, slotKey, target);
    }

    public static void loadSlottedItems(ListTag list, String slotKey, NonNullList<ItemStack> target) {
        HbmItemStackUtil.loadSlottedItems(list, slotKey, target);
    }

    public static void loadSlottedItems(ListTag list, String slotKey, ItemStackHandler target) {
        HbmItemStackUtil.loadSlottedItems(list, slotKey, target);
    }

    public static void loadSlottedItems(ListTag list, String slotKey, Container target) {
        HbmItemStackUtil.loadSlottedItems(list, slotKey, target);
    }

    public static List<ItemStack> clearToDrops(ItemStack[] items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(NonNullList<ItemStack> items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(IItemHandler items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(ItemStackHandler items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static List<ItemStack> clearToDrops(Container items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    public static void spillItems(Level level, BlockPos pos, ItemStack[] items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, NonNullList<ItemStack> items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, IItemHandler items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillItems(Level level, BlockPos pos, Container items, RandomSource random) {
        HbmItemStackUtil.spillItems(level, pos, items, random);
    }

    public static void spillStack(Level level, BlockPos pos, ItemStack stack, RandomSource random) {
        HbmItemStackUtil.spillStack(level, pos, stack, random);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack) {
        return HbmItemStackUtil.giveOrDrop(player, stack);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, boolean throwRandomly) {
        return HbmItemStackUtil.giveOrDrop(player, stack, throwRandomly);
    }

    public static boolean giveOrDrop(Player player, ItemStack stack, Level fallbackLevel, double x, double y, double z) {
        return HbmItemStackUtil.giveOrDrop(player, stack, fallbackLevel, x, y, z);
    }

    public static void giveChanceStacksToPlayer(Player player, List<HbmItemStackUtil.ChanceStack> stacks) {
        HbmItemStackUtil.giveChanceStacksToPlayer(player, stacks, player == null ? null : player.getRandom());
    }

    public static void giveChanceStacksToPlayer(Player player, List<HbmItemStackUtil.ChanceStack> stacks,
            RandomSource random) {
        HbmItemStackUtil.giveChanceStacksToPlayer(player, stacks, random);
    }
}
