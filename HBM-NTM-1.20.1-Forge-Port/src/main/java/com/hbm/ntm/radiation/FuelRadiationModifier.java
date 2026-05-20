package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FuelRadiationModifier implements HazardModifier {
    private final float target;

    public FuelRadiationModifier(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return level;
        }
        double depletion = Math.pow((double) stack.getDamageValue() / stack.getMaxDamage(), 0.4D);
        return (float) (level + (target - level) * depletion);
    }
}
