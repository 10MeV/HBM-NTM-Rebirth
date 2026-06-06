package com.hbm.ntm.api.entity;

import net.minecraft.world.damagesource.DamageSource;

/**
 * Modern counterpart of the legacy IResistanceProvider contract.
 */
public interface ResistanceProvider {
    float[] getCurrentDtDr(DamageSource damage, float amount, float pierceDt, float pierceDr);

    void onDamageDealt(DamageSource damage, float amount);
}
