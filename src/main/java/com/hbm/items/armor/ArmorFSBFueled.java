package com.hbm.items.armor;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.item.FsbFueledArmorItem;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Legacy package carrier for fluid-fueled full-set armor.
 */
@Deprecated(forRemoval = false)
public class ArmorFSBFueled extends FsbFueledArmorItem {
    public ArmorFSBFueled(ArmorMaterial material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, FluidType fuelType, int maxFuel, int fillRate, int consumption,
            int drain, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, fuelType, maxFuel, fillRate, consumption, drain,
                fullSetTraits);
    }

    public ArmorFSBFueled(ArmorMaterial material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, int maxFuel, int fillRate, int consumption, int drain,
            FluidType... acceptedFuelTypes) {
        super(material, type, properties, fullSetEffects, maxFuel, fillRate, consumption, drain,
                acceptedFuelTypes);
    }

    public ArmorFSBFueled(ArmorMaterial material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, int maxFuel, int fillRate, int consumption, int drain,
            FullSetTraits fullSetTraits, FluidType... acceptedFuelTypes) {
        super(material, type, properties, fullSetEffects, maxFuel, fillRate, consumption, drain,
                fullSetTraits, acceptedFuelTypes);
    }

    public ArmorFSBFueled(ArmorMaterial material, int slot, String texture, FluidType fuelType, int maxFuel,
            int fillRate, int consumption, int drain) {
        this(material, typeFor(slot), new Properties(), List.of(), fuelType, maxFuel, fillRate, consumption, drain,
                FullSetTraits.NONE);
    }

    protected static ArmorItem.Type typeFor(int slot) {
        return switch (slot) {
            case 0 -> ArmorItem.Type.HELMET;
            case 1 -> ArmorItem.Type.CHESTPLATE;
            case 2 -> ArmorItem.Type.LEGGINGS;
            case 3 -> ArmorItem.Type.BOOTS;
            default -> throw new IllegalArgumentException("Unknown legacy armor slot: " + slot);
        };
    }
}
