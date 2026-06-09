package com.hbm.ntm.api.entity;

import net.minecraft.world.damagesource.DamageSource;

/**
 * Legacy-name bridge for custom entity DT/DR providers.
 */
@Deprecated(forRemoval = false)
public interface IResistanceProvider extends ResistanceProvider {
    float[] getCurrentDTDR(DamageSource damage, float amount, float pierceDT, float pierce);

    @Override
    default float[] getCurrentDtDr(DamageSource damage, float amount, float pierceDt, float pierceDr) {
        return getCurrentDTDR(damage, amount, pierceDt, pierceDr);
    }
}
