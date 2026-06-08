package com.hbm.ntm.util;

import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class HbmWeightedRandomUtil {
    private HbmWeightedRandomUtil() {
    }

    @Nullable
    public static <T extends WeightedEntry> T getRandomItem(RandomSource random, List<T> entries) {
        int totalWeight = totalWeight(entries);
        if (totalWeight <= 0) {
            return null;
        }
        return getWeightedItem(entries, random.nextInt(totalWeight));
    }

    @Nullable
    public static <T extends WeightedEntry> T getRandomItem(Random random, List<T> entries) {
        int totalWeight = totalWeight(entries);
        if (totalWeight <= 0) {
            return null;
        }
        return getWeightedItem(entries, random.nextInt(totalWeight));
    }

    public static int totalWeight(List<? extends WeightedEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (WeightedEntry entry : entries) {
            if (entry != null && entry.itemWeight() > 0) {
                total += entry.itemWeight();
            }
        }
        return total;
    }

    @Nullable
    public static <T extends WeightedEntry> T getWeightedItem(List<T> entries, int weightRoll) {
        Objects.requireNonNull(entries, "entries");
        int remaining = weightRoll;
        for (T entry : entries) {
            if (entry == null || entry.itemWeight() <= 0) {
                continue;
            }
            remaining -= entry.itemWeight();
            if (remaining < 0) {
                return entry;
            }
        }
        return null;
    }

    public interface WeightedEntry {
        int itemWeight();
    }
}
