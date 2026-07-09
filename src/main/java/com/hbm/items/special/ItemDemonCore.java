package com.hbm.items.special;

import com.hbm.ntm.item.DemonCoreItem;
import net.minecraft.world.item.Item;

/**
 * Legacy 1.7.10 package bridge for the open demon core item.
 */
@Deprecated(forRemoval = false)
public class ItemDemonCore extends DemonCoreItem {
    public ItemDemonCore() {
        this(new Item.Properties());
    }

    public ItemDemonCore(Item.Properties properties) {
        super(properties);
    }
}
