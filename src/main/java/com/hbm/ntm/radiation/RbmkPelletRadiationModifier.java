package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RbmkPelletRadiationModifier implements HazardModifier {
    private final float target;
    private final Integer legacyMeta;

    public RbmkPelletRadiationModifier(float target) {
        this.target = target;
        this.legacyMeta = null;
    }

    public RbmkPelletRadiationModifier(float target, int legacyMeta) {
        this.target = target;
        this.legacyMeta = legacyMeta;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        int meta = rectify(legacyMeta == null ? stack.getDamageValue() : legacyMeta);
        float depletion = (meta % 5) / 4.0F;
        float modified = level + (target - level) * depletion;
        if (meta >= 5) {
            modified += RadiationConstants.XE135 * RadiationConstants.NUGGET;
        }
        return modified;
    }

    private static int rectify(int meta) {
        return Math.abs(meta) % 10;
    }
}
