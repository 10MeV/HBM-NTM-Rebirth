package com.hbm.hazard.modifier;

import com.hbm.ntm.radiation.RbmkPelletRadiationModifier;
import com.hbm.ntm.radiation.RbmkRadiationModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Deprecated(forRemoval = false)
public class HazardModifierRBMKRadiation extends HazardModifier {
    private final RbmkRadiationModifier rodDelegate;
    private final float target;

    public HazardModifierRBMKRadiation(float target, boolean linear) {
        this.target = target;
        this.rodDelegate = new RbmkRadiationModifier(target, linear);
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        CompoundTag tag = stack.getTag();
        if (tag != null && (tag.contains("enrichment") || tag.contains("yield") || tag.contains("poison") || tag.contains("xenon"))) {
            return rodDelegate.modify(stack, holder, level);
        }
        return new RbmkPelletRadiationModifier(target).modify(stack, holder, level);
    }
}
