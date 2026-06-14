package com.hbm.hazard.modifier;

import com.hbm.ntm.radiation.FuelRadiationModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class HazardModifierFuelRadiation extends HazardModifier {
    private final FuelRadiationModifier delegate;

    public HazardModifierFuelRadiation(float target) {
        this.delegate = new FuelRadiationModifier(target);
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        return delegate.modify(stack, holder, level);
    }
}
