package com.hbm.items.armor;

import com.hbm.ntm.item.EuphemiumArmorItem;
import net.minecraft.world.item.ArmorItem;

/**
 * Legacy package carrier for the Euphemium armor set.
 *
 * <p>The full-set effects, durability boundary and rarity remain in the
 * single modern {@link EuphemiumArmorItem} runtime.</p>
 */
@Deprecated(forRemoval = false)
public class ArmorEuphemium extends EuphemiumArmorItem {
    public ArmorEuphemium(ArmorItem.Type type, Properties properties) {
        super(type, properties);
    }
}
