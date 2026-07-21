package com.hbm.items.armor;

import com.hbm.ntm.item.DnsArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.List;
import net.minecraft.world.item.ArmorItem;

/**
 * Legacy package carrier for the DNT armor family, whose source registry IDs
 * are the historical {@code dns_*} names.
 */
@Deprecated(forRemoval = false)
public class ArmorDNT extends DnsArmorItem {
    public ArmorDNT(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain,
                fullSetTraits);
    }
}
