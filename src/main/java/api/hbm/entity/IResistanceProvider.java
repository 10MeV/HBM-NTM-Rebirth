package api.hbm.entity;

import net.minecraft.world.damagesource.DamageSource;

/**
 * Legacy 1.7.10 package bridge for custom entity DT/DR providers.
 */
@Deprecated(forRemoval = false)
public interface IResistanceProvider extends com.hbm.ntm.api.entity.IResistanceProvider {
    @Override
    float[] getCurrentDTDR(DamageSource damage, float amount, float pierceDT, float pierce);
}
