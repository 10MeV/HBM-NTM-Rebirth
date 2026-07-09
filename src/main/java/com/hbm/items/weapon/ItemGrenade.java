package com.hbm.items.weapon;

import com.hbm.ntm.item.DynamiteStickItem;
import net.minecraft.world.item.Item;

public class ItemGrenade extends DynamiteStickItem {
    public int fuse = 4;

    public ItemGrenade(int fuse) {
        this(fuse, new Item.Properties());
    }

    public ItemGrenade(int fuse, Item.Properties properties) {
        super(properties.stacksTo(16));
        this.fuse = fuse;
    }

    public static int getFuseTicks(Item grenade) {
        return ((ItemGrenade) grenade).fuse * 20;
    }
}
