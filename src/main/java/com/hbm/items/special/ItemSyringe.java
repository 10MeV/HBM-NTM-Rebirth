package com.hbm.items.special;

import com.hbm.ntm.item.LegacySyringeItem;
import net.minecraft.world.item.Item;

/**
 * Legacy 1.7.10 package bridge for ItemSyringe-backed medical syringes.
 *
 * <p>The original class used item identity checks against split registry
 * fields. Modern registrations keep that split and pass the legacy branch
 * through {@link LegacySyringeItem.Kind} while preserving the old FQCN.</p>
 */
@Deprecated(forRemoval = false)
public class ItemSyringe extends LegacySyringeItem {
    public ItemSyringe(Item.Properties properties, LegacySyringeItem.Kind kind) {
        super(properties, kind);
    }
}
