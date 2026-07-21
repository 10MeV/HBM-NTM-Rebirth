package com.hbm.items.armor;

import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.List;
import net.minecraft.world.item.ArmorItem;

/** Legacy carrier for Bismuth full-set armor and its existing OBJ renderer. */
@Deprecated(forRemoval = false)
public class ArmorBismuth extends ArmorFSB {
    public ArmorBismuth(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, boolean noHelmet, int dashCount, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, noHelmet, dashCount, fullSetTraits);
    }
}
