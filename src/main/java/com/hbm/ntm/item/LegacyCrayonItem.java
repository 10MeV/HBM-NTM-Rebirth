package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

/** One split modern registry entry for a legacy coloured crayon metadata value. */
public class LegacyCrayonItem extends Item {
    private final int tintColor;

    public LegacyCrayonItem(Properties properties, int tintColor) {
        super(properties);
        this.tintColor = tintColor;
    }

    public int getTintColor(int tintIndex) {
        return tintIndex == 1 ? tintColor : 0xFFFFFF;
    }
}
