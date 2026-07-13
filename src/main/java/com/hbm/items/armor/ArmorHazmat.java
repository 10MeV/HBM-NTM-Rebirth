package com.hbm.items.armor;

import com.hbm.ntm.item.HazmatArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Legacy package facade for the 1.7.10 full-set hazmat armor pieces.
 */
@Deprecated(forRemoval = false)
public class ArmorHazmat extends HazmatArmorItem {
    public ArmorHazmat(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    public ArmorHazmat(ArmorMaterial material, int slot, String texture) {
        this(material, typeFor(slot), new Properties());
    }

    protected static ArmorItem.Type typeFor(int slot) {
        return switch (slot) {
            case 0 -> ArmorItem.Type.HELMET;
            case 1 -> ArmorItem.Type.CHESTPLATE;
            case 2 -> ArmorItem.Type.LEGGINGS;
            case 3 -> ArmorItem.Type.BOOTS;
            default -> throw new IllegalArgumentException("Unknown legacy armor slot: " + slot);
        };
    }
}
