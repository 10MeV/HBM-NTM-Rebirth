package com.hbm.items.tool;

import com.hbm.ntm.item.PollutionDetectorItem;
import net.minecraft.world.item.Item;

/**
 * Old-package source migration facade for the legacy pollution detector item.
 */
@Deprecated(forRemoval = false)
public class ItemPollutionDetector extends PollutionDetectorItem {
    public ItemPollutionDetector() {
        this(new Item.Properties().stacksTo(1));
    }

    public ItemPollutionDetector(Item.Properties properties) {
        super(properties);
    }
}
