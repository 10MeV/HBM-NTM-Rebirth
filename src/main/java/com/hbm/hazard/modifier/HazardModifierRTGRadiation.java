package com.hbm.hazard.modifier;

import com.hbm.ntm.radiation.RtgRadiationModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class HazardModifierRTGRadiation extends HazardModifier {
    private final RtgRadiationModifier delegate;

    public HazardModifierRTGRadiation(float target) {
        this.delegate = new RtgRadiationModifier(target);
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        return delegate.modify(stack, holder, level);
    }
}
