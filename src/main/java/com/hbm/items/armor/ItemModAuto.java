package com.hbm.items.armor;

import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;

/** Legacy carrier; auto-injector behavior remains in {@link ArmorModItems.AutoInjector}. */
@Deprecated(forRemoval = false)
public class ItemModAuto extends ArmorModItems.AutoInjector {
    public ItemModAuto() {
        super(new Item.Properties());
    }
}
