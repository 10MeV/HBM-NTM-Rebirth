package com.hbm.ntm.api.entity;

import net.minecraft.world.damagesource.DamageSource;

/**
 * Modern counterpart of the legacy IResistanceProvider contract.
 */
public interface ResistanceProvider {
    default float[] getCurrentDtDr(DamageSource damage, float amount, float pierceDt, float pierceDr) {
        return new float[] { 0.0F, 0.0F };
    }

    default void onDamageDealt(DamageSource damage, float amount) {
    }
}
