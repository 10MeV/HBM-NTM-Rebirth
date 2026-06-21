package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

public class ShredderBladeItem extends Item {
    public ShredderBladeItem(int durability) {
        super(new Item.Properties().stacksTo(1).durability(durability));
    }
}
