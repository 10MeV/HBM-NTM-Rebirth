package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

public class ItemMachineUpgrade extends Item {
    private final UpgradeType type;
    private final int tier;

    public ItemMachineUpgrade(Properties properties, UpgradeType type, int tier) {
        super(properties.stacksTo(1));
        this.type = type;
        this.tier = Math.max(0, tier);
    }

    public UpgradeType getUpgradeType() {
        return type;
    }

    public int getTier() {
        return tier;
    }

    public enum UpgradeType {
        SPEED,
        EFFECT,
        POWER,
        OVERDRIVE
    }
}
