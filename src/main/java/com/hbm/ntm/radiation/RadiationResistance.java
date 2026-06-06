package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class RadiationResistance {
    public static float calculateRadiationModifier(LivingEntity entity) {
        return HazmatRegistry.calculateRadiationModifier(entity);
    }

    private RadiationResistance() {
    }
}
