package com.hbm.ntm.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class HbmItemStackUtil {
    public static final String LEGACY_ITEMS_TAG = "items";
    public static final String LEGACY_SLOT_TAG = "slot";

    private HbmItemStackUtil() {
    }

    public static ItemStack carefulCopy(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    public static ItemStack carefulCopyWithSize(ItemStack stack, int size) {
        if (stack == null || stack.isEmpty() || size <= 0) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(size);
    }

    public static ItemStack[] carefulCopyArray(ItemStack[] array) {
        return array == null ? null : carefulCopyArray(array, 0, array.length - 1);
    }

    public static ItemStack[] carefulCopyArray(ItemStack[] array, int start, int end) {
        if (array == null) {
            return null;
        }
        ItemStack[] copy = new ItemStack[array.length];
        for (int slot = Math.max(0, start); slot <= Math.min(end, array.length - 1); slot++) {
            copy[slot] = carefulCopy(array[slot]);
        }
        return copy;
    }

    public static ItemStack[] carefulCopyArrayTruncate(ItemStack[] array, int start, int end) {
        if (array == null || end < start) {
            return null;
        }
        int from = Math.max(0, start);
        int to = Math.min(end, array.length - 1);
        if (to < from) {
            return new ItemStack[0];
        }
        ItemStack[] copy = new ItemStack[to - from + 1];
        for (int index = 0; index < copy.length; index++) {
            copy[index] = carefulCopy(array[from + index]);
        }
        return copy;
    }

    public static NonNullList<ItemStack> carefulCopyList(NonNullList<ItemStack> stacks) {
        NonNullList<ItemStack> copy = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < stacks.size(); slot++) {
            copy.set(slot, carefulCopy(stacks.get(slot)));
        }
        return copy;
    }

    public static boolean areStacksCompatible(ItemStack first, ItemStack second) {
        if (first == null || second == null) {
            return first == second;
        }
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        return ItemStack.isSameItemSameTags(first, second);
    }

    public static boolean doesStackDataMatch(ItemStack first, ItemStack second) {
        return areStacksCompatible(first, second);
    }

    public static ItemStack addTooltipToStack(ItemStack stack, String... lines) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag display = new CompoundTag();
        ListTag lore = new ListTag();
        for (String line : lines) {
            Component text = Component.literal(line == null ? "" : line).withStyle(ChatFormatting.GRAY);
            lore.add(StringTag.valueOf(Component.Serializer.toJson(text)));
        }
        display.put("Lore", lore);
        tag.put("display", display);
        return stack;
    }

    public static ItemStack addStackSizeLabel(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.getCount() > 64) {
            int stacks = stack.getCount() / 64;
            int items = stack.getCount() % 64;
            addTooltipToStack(stack, ChatFormatting.RED + "" + stacks + "x64" + (items > 0 ? " + " + items : ""));
        }
        return stack;
    }

    public static void addStacksToNbt(ItemStack container, ItemStack... stacks) {
        if (container == null || container.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
        for (int slot = 0; slot < stacks.length; slot++) {
            items.set(slot, carefulCopy(stacks[slot]));
        }
        container.getOrCreateTag().put(LEGACY_ITEMS_TAG, saveLegacyItems(items).getList(LEGACY_ITEMS_TAG, Tag.TAG_COMPOUND));
    }

    public static void addStacksToNBT(ItemStack container, ItemStack... stacks) {
        addStacksToNbt(container, stacks);
    }

    public static void addStacksToNbt(ItemStack container, List<ItemStack> stacks) {
        addStacksToNbt(container, stacks.toArray(ItemStack[]::new));
    }

    public static boolean hasLegacyItemsTag(ItemStack container) {
        return container != null && !container.isEmpty() && container.hasTag()
                && container.getTag().contains(LEGACY_ITEMS_TAG, Tag.TAG_LIST);
    }

    public static NonNullList<ItemStack> readStacksFromNbt(ItemStack container) {
        int count = 0;
        if (container != null && container.hasTag()) {
            count = container.getTag().getList(LEGACY_ITEMS_TAG, Tag.TAG_COMPOUND).size();
        }
        return readStacksFromNbt(container, count);
    }

    public static NonNullList<ItemStack> readStacksFromNbt(ItemStack container, int count) {
        if (container == null || container.isEmpty() || !container.hasTag()) {
            return NonNullList.withSize(Math.max(0, count), ItemStack.EMPTY);
        }
        return loadLegacyItems(container.getTag(), count);
    }

    public static ItemStack[] readStackArrayFromNbt(ItemStack container) {
        NonNullList<ItemStack> stacks = readStacksFromNbt(container);
        return stacks.toArray(ItemStack[]::new);
    }

    public static ItemStack[] readStackArrayFromNbt(ItemStack container, int count) {
        NonNullList<ItemStack> stacks = readStacksFromNbt(container, count);
        return stacks.toArray(ItemStack[]::new);
    }

    public static ItemStack[] readStacksFromNBT(ItemStack container) {
        return readStackArrayFromNbt(container);
    }

    public static ItemStack[] readStacksFromNBT(ItemStack container, int count) {
        return readStackArrayFromNbt(container, count);
    }

    public static CompoundTag saveLegacyItems(ItemStackHandler items) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(items.getSlots(), ItemStack.EMPTY);
        for (int slot = 0; slot < items.getSlots(); slot++) {
            stacks.set(slot, items.getStackInSlot(slot));
        }
        return saveLegacyItems(stacks);
    }

    public static CompoundTag saveLegacyItems(NonNullList<ItemStack> items) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stackTag.putByte(LEGACY_SLOT_TAG, (byte) slot);
                stack.save(stackTag);
                list.add(stackTag);
            }
        }
        tag.put(LEGACY_ITEMS_TAG, list);
        return tag;
    }

    public static void loadLegacyItems(CompoundTag tag, ItemStackHandler items) {
        NonNullList<ItemStack> stacks = loadLegacyItems(tag, items.getSlots());
        for (int slot = 0; slot < stacks.size(); slot++) {
            items.setStackInSlot(slot, stacks.get(slot));
        }
    }

    public static NonNullList<ItemStack> loadLegacyItems(CompoundTag tag, int slotCount) {
        NonNullList<ItemStack> items = NonNullList.withSize(Math.max(0, slotCount), ItemStack.EMPTY);
        if (tag == null || slotCount <= 0 || !tag.contains(LEGACY_ITEMS_TAG, Tag.TAG_LIST)) {
            return items;
        }
        ListTag list = tag.getList(LEGACY_ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag stackTag = list.getCompound(index);
            int slot = stackTag.getByte(LEGACY_SLOT_TAG) & 255;
            if (slot >= 0 && slot < slotCount) {
                items.set(slot, ItemStack.of(stackTag));
            }
        }
        return items;
    }

    public static List<ItemStack> clearToDrops(ItemStackHandler items) {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    public static void spillItems(Level level, BlockPos pos, IItemHandler items, RandomSource random) {
        if (level == null || pos == null || items == null || level.isClientSide) {
            return;
        }
        RandomSource roll = random == null ? level.random : random;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            spillStack(level, pos, items.getStackInSlot(slot), roll);
        }
    }

    public static void spillItems(Level level, BlockPos pos, NonNullList<ItemStack> items, RandomSource random) {
        if (level == null || pos == null || items == null || level.isClientSide) {
            return;
        }
        RandomSource roll = random == null ? level.random : random;
        for (ItemStack stack : items) {
            spillStack(level, pos, stack, roll);
        }
    }

    public static void spillStack(Level level, BlockPos pos, ItemStack stack, RandomSource random) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemStack remainder = stack.copy();
        float xOffset = random.nextFloat() * 0.8F + 0.1F;
        float yOffset = random.nextFloat() * 0.8F + 0.1F;
        float zOffset = random.nextFloat() * 0.8F + 0.1F;
        while (!remainder.isEmpty()) {
            int split = Math.min(remainder.getCount(), random.nextInt(21) + 10);
            ItemStack dropped = remainder.split(split);
            ItemEntity entity = new ItemEntity(level, pos.getX() + xOffset, pos.getY() + yOffset,
                    pos.getZ() + zOffset, dropped);
            float motion = 0.05F;
            entity.setDeltaMovement(random.nextGaussian() * motion, random.nextGaussian() * motion + 0.2D,
                    random.nextGaussian() * motion);
            level.addFreshEntity(entity);
        }
    }

    public static boolean giveOrDrop(Player player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return true;
        }
        ItemStack copy = stack.copy();
        boolean inserted = player.getInventory().add(copy);
        if (!copy.isEmpty()) {
            player.drop(copy, false);
        }
        return inserted && copy.isEmpty();
    }

    public static void giveChanceStacksToPlayer(Player player, List<ChanceStack> stacks, RandomSource random) {
        if (player == null || stacks == null || stacks.isEmpty()) {
            return;
        }
        RandomSource roll = random == null ? player.getRandom() : random;
        for (ChanceStack chanceStack : stacks) {
            if (chanceStack != null && chanceStack.roll(roll)) {
                giveOrDrop(player, chanceStack.stack());
            }
        }
    }

    public static List<String> getTagNames(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        return stack.getTags()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .toList();
    }

    public static List<ResourceLocation> getTagIds(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        return stack.getTags()
                .map(TagKey::location)
                .toList();
    }

    public static String getModIdFromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? null : id.getNamespace();
    }

    public static void addNbtFromString(ItemStack stack, String nbt) {
        if (stack == null || stack.isEmpty() || nbt == null || nbt.isBlank()) {
            return;
        }
        try {
            stack.setTag(TagParser.parseTag(nbt));
        } catch (CommandSyntaxException ignored) {
        }
    }

    public static boolean isInAnyTag(ItemStack stack, Collection<TagKey<Item>> tags) {
        if (stack == null || stack.isEmpty() || tags == null || tags.isEmpty()) {
            return false;
        }
        for (TagKey<Item> tag : tags) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    public record ChanceStack(ItemStack stack, float chance) {
        public ChanceStack {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            chance = Math.max(0.0F, Math.min(1.0F, chance));
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }

        private boolean roll(RandomSource random) {
            return !stack.isEmpty() && (chance >= 1.0F || random.nextFloat() < chance);
        }
    }
}
