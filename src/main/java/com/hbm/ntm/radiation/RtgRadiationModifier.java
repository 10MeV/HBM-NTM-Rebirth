package com.hbm.ntm.radiation;

import com.hbm.ntm.item.RtgPelletItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RtgRadiationModifier implements HazardModifier {
    private final float target;

    public RtgRadiationModifier(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        if (!(stack.getItem() instanceof RtgPelletItem pellet)) {
            return level;
        }
        double depletion = pellet.getDurabilityForDisplay(stack);
        return (float) (level + (target - level) * depletion);
    }
}
