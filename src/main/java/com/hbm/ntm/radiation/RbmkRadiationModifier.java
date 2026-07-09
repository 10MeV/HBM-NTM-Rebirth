package com.hbm.ntm.radiation;

import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.ntm.neutron.RBMKFuelRodState;
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
        if (!(stack.getItem() instanceof ItemRBMKRod fuelRod)) {
            return level;
        }
        CompoundTag tag = stack.getTag();

        double totalYield = fuelRod.getSpec().totalYield() > 0.0D ? fuelRod.getSpec().totalYield() : initialYield;
        double enrichment = getEnrichment(tag, totalYield);
        double depletion = linear ? 1.0D - enrichment : 1.0D - Math.pow(enrichment, 2.0D);
        double poison = getPoisonLevel(tag);

        float modified = (float) (level + (target - level) * depletion);
        return modified + (float) (RadiationConstants.XE135 * poison);
    }

    private double getEnrichment(CompoundTag tag, double totalYield) {
        double yield = tag == null ? totalYield : tag.getDouble(RBMKFuelRodState.TAG_YIELD);
        return yield / totalYield;
    }

    private double getPoisonLevel(CompoundTag tag) {
        return tag == null ? 0.0D : tag.getDouble(RBMKFuelRodState.TAG_XENON) / 100.0D;
    }
}
