package com.hbm.ntm.recipe;

import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public final class LegacyMachineUpgradeManager {
    private LegacyMachineUpgradeManager() {
    }

    public static Levels checkSlots(ItemStackHandler items, int startSlot, int endSlot, Map<UpgradeType, Integer> validUpgrades) {
        int speed = 0;
        int effect = 0;
        int power = 0;
        int overdrive = 0;
        int afterburn = 0;
        int fortune = 0;
        int smelter = 0;
        int nullifier = 0;
        int shredder = 0;
        int centrifuge = 0;
        int crystallizer = 0;
        for (int slot = startSlot; slot <= endSlot; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!(stack.getItem() instanceof ItemMachineUpgrade upgrade)) {
                continue;
            }
            UpgradeType type = upgrade.getUpgradeType();
            Integer maxLevel = validUpgrades.get(type);
            if (maxLevel == null) {
                continue;
            }
            int tier = upgrade.getTier();
            switch (type) {
                case SPEED -> speed = cappedLevel(speed, tier, maxLevel);
                case EFFECT -> effect = cappedLevel(effect, tier, maxLevel);
                case POWER -> power = cappedLevel(power, tier, maxLevel);
                case OVERDRIVE -> overdrive = cappedLevel(overdrive, tier, maxLevel);
                case AFTERBURN -> afterburn = cappedLevel(afterburn, tier, maxLevel);
                case FORTUNE -> fortune = cappedLevel(fortune, tier, maxLevel);
                case SMELTER -> smelter = cappedLevel(smelter, tier, maxLevel);
                case NULLIFIER -> nullifier = cappedLevel(nullifier, tier, maxLevel);
                case SHREDDER -> shredder = cappedLevel(shredder, tier, maxLevel);
                case CENTRIFUGE -> centrifuge = cappedLevel(centrifuge, tier, maxLevel);
                case CRYSTALLIZER -> crystallizer = cappedLevel(crystallizer, tier, maxLevel);
            }
        }
        if ((speed | effect | power | overdrive | afterburn | fortune | smelter | nullifier | shredder
                | centrifuge | crystallizer) == 0) {
            return Levels.EMPTY;
        }
        return new Levels(speed, effect, power, overdrive, afterburn, fortune, smelter, nullifier, shredder,
                centrifuge, crystallizer);
    }

    private static int cappedLevel(int current, int add, int maxLevel) {
        return Math.min(current + add, maxLevel);
    }

    public static final class SlotCache {
        private final ItemStack[] slots;
        private Levels levels = Levels.EMPTY;
        private boolean dirty = true;

        public SlotCache(int slotCount) {
            if (slotCount <= 0) {
                throw new IllegalArgumentException("slotCount must be positive");
            }
            this.slots = new ItemStack[slotCount];
        }

        public void invalidate() {
            dirty = true;
        }

        public Levels get(ItemStackHandler items, int startSlot, int endSlot, Map<UpgradeType, Integer> validUpgrades) {
            if (endSlot - startSlot + 1 != slots.length) {
                throw new IllegalArgumentException("upgrade slot range does not match cache size");
            }
            if (!dirty && matches(items, startSlot)) {
                return levels;
            }
            levels = checkSlots(items, startSlot, endSlot, validUpgrades);
            snapshot(items, startSlot);
            dirty = false;
            return levels;
        }

        private boolean matches(ItemStackHandler items, int startSlot) {
            for (int i = 0; i < slots.length; i++) {
                ItemStack cached = slots[i];
                if (cached == null || !ItemStack.matches(items.getStackInSlot(startSlot + i), cached)) {
                    return false;
                }
            }
            return true;
        }

        private void snapshot(ItemStackHandler items, int startSlot) {
            for (int i = 0; i < slots.length; i++) {
                ItemStack stack = items.getStackInSlot(startSlot + i);
                slots[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
            }
        }
    }

    public static final class Levels {
        private static final Levels EMPTY = new Levels(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        private final int speed;
        private final int effect;
        private final int power;
        private final int overdrive;
        private final int afterburn;
        private final int fortune;
        private final int smelter;
        private final int nullifier;
        private final int shredder;
        private final int centrifuge;
        private final int crystallizer;

        private Levels(int speed, int effect, int power, int overdrive, int afterburn, int fortune, int smelter,
                int nullifier, int shredder, int centrifuge, int crystallizer) {
            this.speed = speed;
            this.effect = effect;
            this.power = power;
            this.overdrive = overdrive;
            this.afterburn = afterburn;
            this.fortune = fortune;
            this.smelter = smelter;
            this.nullifier = nullifier;
            this.shredder = shredder;
            this.centrifuge = centrifuge;
            this.crystallizer = crystallizer;
        }

        public int getLevel(UpgradeType type) {
            return switch (type) {
                case SPEED -> speed;
                case EFFECT -> effect;
                case POWER -> power;
                case OVERDRIVE -> overdrive;
                case AFTERBURN -> afterburn;
                case FORTUNE -> fortune;
                case SMELTER -> smelter;
                case NULLIFIER -> nullifier;
                case SHREDDER -> shredder;
                case CENTRIFUGE -> centrifuge;
                case CRYSTALLIZER -> crystallizer;
            };
        }

        public Map<UpgradeType, Integer> levels() {
            if (this == EMPTY) {
                return Map.of();
            }
            EnumMap<UpgradeType, Integer> levels = new EnumMap<>(UpgradeType.class);
            putNonZero(levels, UpgradeType.SPEED, speed);
            putNonZero(levels, UpgradeType.EFFECT, effect);
            putNonZero(levels, UpgradeType.POWER, power);
            putNonZero(levels, UpgradeType.OVERDRIVE, overdrive);
            putNonZero(levels, UpgradeType.AFTERBURN, afterburn);
            putNonZero(levels, UpgradeType.FORTUNE, fortune);
            putNonZero(levels, UpgradeType.SMELTER, smelter);
            putNonZero(levels, UpgradeType.NULLIFIER, nullifier);
            putNonZero(levels, UpgradeType.SHREDDER, shredder);
            putNonZero(levels, UpgradeType.CENTRIFUGE, centrifuge);
            putNonZero(levels, UpgradeType.CRYSTALLIZER, crystallizer);
            return Map.copyOf(levels);
        }

        private static void putNonZero(EnumMap<UpgradeType, Integer> levels, UpgradeType type, int level) {
            if (level > 0) {
                levels.put(type, level);
            }
        }
    }
}
