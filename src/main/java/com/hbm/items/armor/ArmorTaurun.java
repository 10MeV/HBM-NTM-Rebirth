package com.hbm.items.armor;

import com.hbm.ntm.item.FsbArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** Legacy package carrier for the non-damageable Taurun FSB armor set. */
@Deprecated(forRemoval = false)
public class ArmorTaurun extends FsbArmorItem {
    public ArmorTaurun(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, boolean noHelmet, int dashCount, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, noHelmet, dashCount, fullSetTraits);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
