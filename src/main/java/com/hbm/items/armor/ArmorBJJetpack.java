package com.hbm.items.armor;

import com.hbm.ntm.item.BjJetpackArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.List;
import net.minecraft.world.item.ArmorItem;

/**
 * Legacy package carrier for the winged BJ powered chestplate.
 *
 * <p>All charge, full-set and flight behavior remains in the modern
 * {@link BjJetpackArmorItem} runtime.</p>
 */
@Deprecated(forRemoval = false)
public class ArmorBJJetpack extends BjJetpackArmorItem {
    public ArmorBJJetpack(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
                          List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate,
                          long consumption, long drain, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain,
                fullSetTraits);
    }
}
