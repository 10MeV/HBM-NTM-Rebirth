package com.hbm.ntm.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RbmkRadiationModifier implements HazardModifier {
    private final float target;
    private final boolean linear;
    private final double initialYield;

    public RbmkRadiationModifier(float target, boolean linear) {
        this(target, linear, 1.0D);
    }

    public RbmkRadiationModifier(float target, boolean linear, double initialYield) {
        this.target = target;
        this.linear = linear;
        this.initialYield = initialYield <= 0.0D ? 1.0D : initialYield;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return level;
        }

        double enrichment = getEnrichment(tag);
        double depletion = linear ? 1.0D - enrichment : 1.0D - Math.pow(enrichment, 2.0D);
        double poison = getPoisonLevel(tag);

        float modified = (float) (level + (target - level) * depletion);
        return modified + (float) (RadiationConstants.XE135 * poison);
    }

    private double getEnrichment(CompoundTag tag) {
        if (tag.contains("enrichment")) {
            return clamp01(tag.getDouble("enrichment"));
        }
        if (tag.contains("yield")) {
            return clamp01(tag.getDouble("yield") / initialYield);
        }
        return 1.0D;
    }

    private double getPoisonLevel(CompoundTag tag) {
        if (tag.contains("poison")) {
            return clamp01(tag.getDouble("poison"));
        }
        if (tag.contains("xenon")) {
            return clamp01(tag.getDouble("xenon") / 100.0D);
        }
        return 0.0D;
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
