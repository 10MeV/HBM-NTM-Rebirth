package com.hbm.items.armor;

import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;

/** Legacy carrier; hazardous-gas scanning remains in {@link ArmorModItems.GasSensor}. */
@Deprecated(forRemoval = false)
public class ItemModSensor extends ArmorModItems.GasSensor {
    public ItemModSensor() {
        super(new Item.Properties());
    }
}
