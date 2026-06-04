package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

public class OreByproductItem extends Item {
    private final int tintColor;

    public OreByproductItem(Properties properties, int tintColor) {
        super(properties);
        this.tintColor = tintColor;
    }

    public int getTintColor(int tintIndex) {
        return tintIndex == 0 ? tintColor : 0xFFFFFF;
    }
}
