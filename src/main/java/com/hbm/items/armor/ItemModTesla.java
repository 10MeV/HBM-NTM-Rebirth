package com.hbm.items.armor;

import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;

/**
 * Legacy package facade for the 1.7.10 back-mounted Tesla armor module.
 *
 * <p>The runtime remains {@link ArmorModItems.BackTesla}; it delegates target
 * selection and electric effects to the shared Tesla block-entity library.</p>
 */
@Deprecated(forRemoval = false)
public class ItemModTesla extends ArmorModItems.BackTesla {
    public ItemModTesla() {
        super(new Item.Properties());
    }
}
