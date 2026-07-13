package com.hbm.items.armor;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.item.DieselSuitArmorItem;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Legacy package carrier for the diesel full-set armor.
 */
@Deprecated(forRemoval = false)
public class ArmorDiesel extends DieselSuitArmorItem {
    public ArmorDiesel(ArmorMaterial material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, int maxFuel, int fillRate, int consumption, int drain,
            FullSetTraits fullSetTraits, FluidType... acceptedFuelTypes) {
        super(material, type, properties, fullSetEffects, maxFuel, fillRate, consumption, drain,
                fullSetTraits, acceptedFuelTypes);
    }

    public ArmorDiesel(ArmorMaterial material, int slot, String texture, FluidType fuelType, int maxFuel,
            int fillRate, int consumption, int drain) {
        this(material, ArmorFSBFueled.typeFor(slot), new Properties(), List.of(), maxFuel, fillRate, consumption,
                drain, FullSetTraits.NONE, fuelType);
    }
}
