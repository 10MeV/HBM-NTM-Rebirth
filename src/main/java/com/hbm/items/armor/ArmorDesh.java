package com.hbm.items.armor;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.item.SteamsuitArmorItem;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Legacy package carrier for the Desh steam full-set armor.
 */
@Deprecated(forRemoval = false)
public class ArmorDesh extends SteamsuitArmorItem {
    public ArmorDesh(ArmorMaterial material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, FluidType fuelType, int maxFuel, int fillRate, int consumption,
            int drain, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, fuelType, maxFuel, fillRate, consumption, drain,
                fullSetTraits);
    }

    public ArmorDesh(ArmorMaterial material, int slot, String texture, FluidType fuelType, int maxFuel,
            int fillRate, int consumption, int drain) {
        this(material, ArmorFSBFueled.typeFor(slot), new Properties(), List.of(), fuelType, maxFuel, fillRate,
                consumption, drain, FullSetTraits.NONE);
    }
}
