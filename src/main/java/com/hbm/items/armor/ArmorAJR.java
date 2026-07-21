package com.hbm.items.armor;

import com.hbm.ntm.item.FsbPoweredArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.List;
import net.minecraft.world.item.ArmorItem;

/** Legacy package carrier for AJR powered FSB armor. */
@Deprecated(forRemoval = false)
public class ArmorAJR extends FsbPoweredArmorItem {
    public ArmorAJR(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain,
                fullSetTraits);
    }
}
