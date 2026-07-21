package com.hbm.items.armor;

import com.hbm.ntm.item.EnvSuitArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.List;
import net.minecraft.world.item.ArmorItem;

/** Legacy package carrier for the EnvSuit powered armor family. */
@Deprecated(forRemoval = false)
public class ArmorEnvsuit extends EnvSuitArmorItem {
    public ArmorEnvsuit(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain);
    }
}
