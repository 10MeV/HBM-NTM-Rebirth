package com.hbm.items.armor;

import com.hbm.ntm.item.BjArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.List;
import net.minecraft.world.item.ArmorItem;

/**
 * Legacy carrier for standard Black Jack powered armor.
 *
 * <p>Charge state and the source-backed incomplete-set helmet punishment stay
 * in the one {@link BjArmorItem} runtime.</p>
 */
@Deprecated(forRemoval = false)
public class ArmorBJ extends BjArmorItem {
    public ArmorBJ(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain,
                fullSetTraits);
    }
}
