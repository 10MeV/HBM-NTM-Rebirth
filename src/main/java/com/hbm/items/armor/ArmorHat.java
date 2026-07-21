package com.hbm.items.armor;

import com.hbm.ntm.item.FabulousHatArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/** Legacy package carrier for the Nossy hat's existing OBJ and DT runtime. */
@Deprecated(forRemoval = false)
public class ArmorHat extends FabulousHatArmorItem {
    public ArmorHat(ArmorMaterial material, Properties properties) {
        super(material, properties);
    }
}
