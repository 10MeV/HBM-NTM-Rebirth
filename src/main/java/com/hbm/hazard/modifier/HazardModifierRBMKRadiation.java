package com.hbm.hazard.modifier;

import com.hbm.items.machine.ItemRBMKPellet;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.ntm.radiation.RbmkPelletRadiationModifier;
import com.hbm.ntm.radiation.RbmkRadiationModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class HazardModifierRBMKRadiation extends HazardModifier {
    private final RbmkRadiationModifier rodDelegate;
    private final RbmkPelletRadiationModifier pelletDelegate;

    public HazardModifierRBMKRadiation(float target, boolean linear) {
        this.rodDelegate = new RbmkRadiationModifier(target, linear);
        this.pelletDelegate = new RbmkPelletRadiationModifier(target);
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        if (stack.getItem() instanceof ItemRBMKRod) {
            return rodDelegate.modify(stack, holder, level);
        }
        if (stack.getItem() instanceof ItemRBMKPellet) {
            return pelletDelegate.modify(stack, holder, level);
        }
        return level;
    }
}
