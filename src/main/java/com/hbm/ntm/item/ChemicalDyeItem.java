package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

public class ChemicalDyeItem extends Item {
    private final int tintColor;

    public ChemicalDyeItem(Properties properties, int tintColor) {
        super(properties);
        this.tintColor = tintColor;
    }

    public int getTintColor(int tintIndex) {
        return tintIndex == 1 ? tintColor : 0xFFFFFF;
    }
}
