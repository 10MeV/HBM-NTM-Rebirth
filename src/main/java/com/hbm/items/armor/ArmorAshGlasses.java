package com.hbm.items.armor;

import com.hbm.ntm.item.ObjArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Legacy carrier for Ash Glasses.
 *
 * <p>The old class only selected {@code ModelGlasses}; the modern OBJ extension
 * remains the sole rendering implementation.</p>
 */
@Deprecated(forRemoval = false)
public class ArmorAshGlasses extends ObjArmorItem {
    public ArmorAshGlasses(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }
}
