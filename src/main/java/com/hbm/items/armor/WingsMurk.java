package com.hbm.items.armor;

import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;

/** Legacy package carrier for the murky wing armor module. */
@Deprecated(forRemoval = false)
public class WingsMurk extends ArmorModItems.Wings {
    public WingsMurk() {
        super(new Item.Properties(), true);
    }
}
