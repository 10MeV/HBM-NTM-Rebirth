package com.hbm.lib;

import com.hbm.ntm.util.HbmWeightedRandomUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Random;

/**
 * Legacy chest-content helper bridge with modern data-only entries.
 */
@Deprecated(forRemoval = false)
public final class HbmChestContents {
    private HbmChestContents() {
    }

    public static ChestContentEntry weighted(ItemLike item, int min, int max, int weight) {
        return weighted(item == null ? ItemStack.EMPTY : new ItemStack(item), min, max, weight);
    }

    public static ChestContentEntry weighted(ItemStack stack, int min, int max, int weight) {
        return new ChestContentEntry(stack, Math.min(min, max), Math.max(min, max), weight);
    }

    public static BookLoreSpec generateOfficeBook(Random random) {
        int roll = random == null ? new Random().nextInt(5) : random.nextInt(5);
        return officeBook(roll);
    }

    public static BookLoreSpec generateOfficeBook(RandomSource random) {
        int roll = random == null ? RandomSource.create().nextInt(5) : random.nextInt(5);
        return officeBook(roll);
    }

    public static BookLoreSpec generateLabBook(Random random) {
        int roll = random == null ? new Random().nextInt(5) : random.nextInt(5);
        return labBook(roll);
    }

    public static BookLoreSpec generateLabBook(RandomSource random) {
        int roll = random == null ? RandomSource.create().nextInt(5) : random.nextInt(5);
        return labBook(roll);
    }

    private static BookLoreSpec officeBook(int roll) {
        return switch (roll) {
            case 0 -> new BookLoreSpec("resignation_note", 3, 0x6BC8FF, 0x0A0A0A);
            case 1 -> new BookLoreSpec("memo_stocks", 1, 0x6BC8FF, 0x0A0A0A);
            case 2 -> new BookLoreSpec("memo_schrab_gsa", 2, 0x6BC8FF, 0x0A0A0A);
            case 3 -> new BookLoreSpec("memo_schrab_rd", 4, 0x6BC8FF, 0x0A0A0A);
            case 4 -> new BookLoreSpec("memo_schrab_nuke", 3, 0x6BC8FF, 0x0A0A0A);
            default -> null;
        };
    }

    private static BookLoreSpec labBook(int roll) {
        return switch (roll) {
            case 0 -> new BookLoreSpec("bf_bomb_1", 4, 0x1E1E1E, 0x46EA44);
            case 1 -> new BookLoreSpec("bf_bomb_2", 6, 0x1E1E1E, 0x46EA44);
            case 2 -> new BookLoreSpec("bf_bomb_3", 6, 0x1E1E1E, 0x46EA44);
            case 3 -> new BookLoreSpec("bf_bomb_4", 5, 0x1E1E1E, 0x46EA44);
            case 4 -> new BookLoreSpec("bf_bomb_5", 9, 0x1E1E1E, 0x46EA44);
            default -> null;
        };
    }

    public record ChestContentEntry(ItemStack stack, int minCount, int maxCount, int itemWeight)
            implements HbmWeightedRandomUtil.WeightedEntry {
        public ChestContentEntry {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }

        public ItemStack rollStack(RandomSource random) {
            ItemStack result = stack();
            if (result.isEmpty()) {
                return result;
            }
            int min = Math.max(0, minCount);
            int max = Math.max(min, maxCount);
            int count = min == max ? min : min + (random == null ? RandomSource.create() : random).nextInt(max - min + 1);
            result.setCount(count);
            return result;
        }
    }

    public record BookLoreSpec(String key, int pages, int coverColor, int textColor) {
        public String nameKey() {
            return "book_lore." + key + ".name";
        }

        public String authorKey() {
            return "book_lore." + key + ".author";
        }

        public String pageKey(int page) {
            return "book_lore." + key + ".page." + page;
        }
    }
}
