package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RtgRadiationModifier implements HazardModifier {
    private final float target;

    public RtgRadiationModifier(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return level;
        }
        double depletion = (double) stack.getDamageValue() / stack.getMaxDamage();
        return (float) (level + (target - level) * depletion);
    }
}
