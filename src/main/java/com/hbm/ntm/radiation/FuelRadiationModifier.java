package com.hbm.ntm.radiation;

import com.hbm.items.machine.ItemZirnoxRod;
import com.hbm.ntm.recipe.ResearchReactorFuelRuntime;
import java.util.OptionalDouble;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FuelRadiationModifier implements HazardModifier {
    private final float target;

    public FuelRadiationModifier(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        OptionalDouble researchFuelDepletion = ResearchReactorFuelRuntime.durabilityForDisplay(stack);
        if (researchFuelDepletion.isPresent()) {
            double depletion = Math.pow(researchFuelDepletion.getAsDouble(), 0.4D);
            return (float) (level + (target - level) * depletion);
        }
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return level;
        }
        int life = stack.getItem() instanceof ItemZirnoxRod ? ItemZirnoxRod.getLifeTime(stack) : stack.getDamageValue();
        double depletion = Math.pow((double) life / (double) maxDamage, 0.4D);
        return (float) (level + (target - level) * depletion);
    }
}
