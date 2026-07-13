package com.hbm.items.armor;

import com.hbm.ntm.item.LiquidatorArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Legacy package carrier for the 1.7.10 liquidator full-set armor.
 */
@Deprecated(forRemoval = false)
public class ArmorLiquidator extends LiquidatorArmorItem {
    public ArmorLiquidator(ArmorItem.Type type, Properties properties) {
        super(type, properties);
    }

    public ArmorLiquidator(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    public ArmorLiquidator(ArmorMaterial material, int slot, String texture) {
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
