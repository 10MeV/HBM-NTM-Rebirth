package com.hbm.hazard.modifier;

import com.hbm.ntm.radiation.RbmkHotModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class HazardModifierRBMKHot extends HazardModifier {
    private final RbmkHotModifier delegate = new RbmkHotModifier();

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        return delegate.modify(stack, holder, level);
    }
}
