package com.hbm.ntm.radiation;

import com.hbm.ntm.item.ZirnoxRodItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FuelRadiationModifier implements HazardModifier {
    private final float target;

    public FuelRadiationModifier(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return level;
        }
        int life = stack.getItem() instanceof ZirnoxRodItem ? ZirnoxRodItem.getLifeTime(stack) : stack.getDamageValue();
        double depletion = Math.pow((double) life / (double) maxDamage, 0.4D);
        return (float) (level + (target - level) * depletion);
    }
}
