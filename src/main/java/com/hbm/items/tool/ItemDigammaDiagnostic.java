package com.hbm.items.tool;

import com.hbm.ntm.item.DigammaDiagnosticItem;
import net.minecraft.world.item.Item;

/**
 * Old-package source migration facade for the legacy digamma diagnostic item.
 */
@Deprecated(forRemoval = false)
public class ItemDigammaDiagnostic extends DigammaDiagnosticItem {
    public ItemDigammaDiagnostic() {
        this(new Item.Properties().stacksTo(1));
    }

    public ItemDigammaDiagnostic(Item.Properties properties) {
        super(properties);
    }
}
