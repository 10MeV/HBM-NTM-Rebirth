package com.hbm.ntm.item;

import com.hbm.items.armor.ArmorFSB;
import net.minecraft.world.item.ArmorMaterial;
import java.util.List;

/**
 * Base armor for the legacy hazmat sets. The 1.7.10 ArmorHazmat class extended
 * ArmorFSB, so every piece must participate in the full-set contract.
 */
public class HazmatArmorItem extends ArmorFSB {
    public HazmatArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties, List.of());
    }
}
