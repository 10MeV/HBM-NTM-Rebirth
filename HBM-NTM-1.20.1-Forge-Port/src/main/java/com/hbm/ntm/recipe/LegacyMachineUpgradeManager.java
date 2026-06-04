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
        EnumMap<UpgradeType, Integer> levels = new EnumMap<>(UpgradeType.class);
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
            int before = levels.getOrDefault(type, 0);
            levels.put(type, Math.min(before + upgrade.getTier(), maxLevel));
        }
        return new Levels(Map.copyOf(levels));
    }

    public record Levels(Map<UpgradeType, Integer> levels) {
        public int getLevel(UpgradeType type) {
            return levels.getOrDefault(type, 0);
        }
    }
}
