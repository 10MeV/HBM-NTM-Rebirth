package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record HazardEntry(HazardType type, float level, List<HazardModifier> modifiers) {
    public HazardEntry {
        level = finiteOrZero(level);
        if (modifiers == null) {
            modifiers = new ArrayList<>();
        }
    }

    public HazardEntry(HazardType type, float level) {
        this(type, level, new ArrayList<>());
    }

    public HazardEntry withModifier(HazardModifier modifier) {
        modifiers.add(modifier);
        return this;
    }

    public float modifiedLevel(ItemStack stack, LivingEntity holder) {
        return finiteOrZero(HazardModifier.applyAll(stack, holder, level, modifiers));
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0.0F;
    }
}
