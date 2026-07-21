package com.hbm.items.armor;

import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;

/** Legacy carrier; ballistic-gauntlet behavior remains in {@link ArmorModItems.BallisticGauntlet}. */
@Deprecated(forRemoval = false)
public class ItemModTwoKick extends ArmorModItems.BallisticGauntlet {
    public ItemModTwoKick() {
        super(new Item.Properties());
    }
}
