package com.hbm.items.special;

import net.minecraft.world.item.Item;

/**
 * Legacy 1.7.10 package bridge for holotape metadata item families.
 */
@Deprecated(forRemoval = false)
public class ItemHoloTape extends Item {
    public ItemHoloTape() {
        this(new Item.Properties());
    }

    public ItemHoloTape(Properties properties) {
        super(properties.stacksTo(1));
    }

    public ItemHoloTape(Class<? extends Enum<?>> theEnum, boolean multiName, boolean multiTexture) {
        this(new Item.Properties());
    }
}
