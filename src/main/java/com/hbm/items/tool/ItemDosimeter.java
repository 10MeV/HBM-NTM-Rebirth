package com.hbm.items.tool;

import com.hbm.ntm.item.DosimeterItem;
import net.minecraft.world.item.Item;

/**
 * Old-package source migration facade for the legacy dosimeter item.
 */
@Deprecated(forRemoval = false)
public class ItemDosimeter extends DosimeterItem {
    public ItemDosimeter() {
        this(new Item.Properties().stacksTo(1));
    }

    public ItemDosimeter(Item.Properties properties) {
        super(properties);
    }
}
