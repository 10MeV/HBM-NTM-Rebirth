package com.hbm.items.special;

import com.hbm.ntm.item.LegacyNuclearWasteItem;
import net.minecraft.world.item.Item;

/**
 * Legacy 1.7.10 package bridge for generic nuclear waste items.
 */
@Deprecated(forRemoval = false)
public class ItemNuclearWaste extends LegacyNuclearWasteItem {
    public ItemNuclearWaste() {
        this(new Item.Properties());
    }

    public ItemNuclearWaste(Item.Properties properties) {
        super(properties);
    }
}
