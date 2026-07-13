package com.hbm.items.armor;

import com.hbm.ntm.item.FsbArmorItem;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Legacy package carrier for armor with a full-set bonus.
 */
@Deprecated(forRemoval = false)
public class ArmorFSB extends FsbArmorItem {
    public ArmorFSB(ArmorMaterial material, ArmorItem.Type type, Properties properties,
                    List<FullSetEffect> fullSetEffects, boolean noHelmet, int dashCount,
                    FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, noHelmet, dashCount, fullSetTraits);
    }

    public ArmorFSB(ArmorMaterial material, ArmorItem.Type type, Properties properties,
                    List<FullSetEffect> fullSetEffects) {
        super(material, type, properties, fullSetEffects);
    }
}
