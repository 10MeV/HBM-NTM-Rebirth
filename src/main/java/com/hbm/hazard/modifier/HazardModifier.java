package com.hbm.hazard.modifier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Legacy package facade for 1.7.10 hazard modifiers.
 */
@Deprecated(forRemoval = false)
public abstract class HazardModifier {
    public abstract float modify(ItemStack stack, LivingEntity holder, float level);

    public static float evalAllModifiers(ItemStack stack, LivingEntity entity, float level, List<HazardModifier> mods) {
        for (HazardModifier mod : mods) {
            level = mod.modify(stack, entity, level);
        }
        return level;
    }
}
