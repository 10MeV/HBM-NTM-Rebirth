package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;

public final class RadiationResistance {
    public static float calculateRadiationModifier(LivingEntity entity) {
        return HazmatRegistry.calculateRadiationModifier(entity);
    }

    public static float calculateRadiationMod(LivingEntity entity) {
        return HazmatRegistry.calculateRadiationMod(entity);
    }

    private RadiationResistance() {
    }
}
