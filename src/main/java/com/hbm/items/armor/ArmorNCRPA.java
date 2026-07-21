package com.hbm.items.armor;

import com.hbm.ntm.item.HbmArmorMaterials;
import com.hbm.ntm.item.NcrpaArmorItem;
import java.util.List;
import net.minecraft.world.item.ArmorItem;

/** Legacy package carrier for NCRPA powered armor. */
@Deprecated(forRemoval = false)
public class ArmorNCRPA extends NcrpaArmorItem {
    public ArmorNCRPA(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain,
                fullSetTraits);
    }
}
