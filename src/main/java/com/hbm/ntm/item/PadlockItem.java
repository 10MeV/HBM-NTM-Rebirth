package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

public class PadlockItem extends KeyPinItem {
    private final double lockMod;

    public PadlockItem(Item.Properties properties, double lockMod) {
        super(properties, true);
        this.lockMod = lockMod;
    }

    public double lockMod() {
        return lockMod;
    }
}
