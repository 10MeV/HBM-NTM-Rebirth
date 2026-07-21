package com.hbm.items.armor;

import com.hbm.ntm.item.HbmArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

/** Legacy carrier for the registered iron-equivalent Mask of Infamy helmet. */
@Deprecated(forRemoval = false)
public class MaskOfInfamy extends ArmorItem {
    public MaskOfInfamy() {
        super(HbmArmorMaterials.MASK_OF_INFAMY, Type.HELMET, new Item.Properties());
    }
}
