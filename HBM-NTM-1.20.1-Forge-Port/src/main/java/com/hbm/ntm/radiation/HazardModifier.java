package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface HazardModifier {
    float modify(ItemStack stack, LivingEntity holder, float level);

    static float applyAll(ItemStack stack, LivingEntity holder, float level, List<HazardModifier> modifiers) {
        float modified = level;
        for (HazardModifier modifier : modifiers) {
            modified = modifier.modify(stack, holder, modified);
        }
        return modified;
    }
}
