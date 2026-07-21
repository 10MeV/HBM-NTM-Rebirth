package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;

/** Legacy facade over the modern weapon explosion effect. */
@Deprecated(forRemoval = false)
public class ExplosionEffectWeapon extends com.hbm.ntm.explosion.vnt.standard.ExplosionEffectWeapon implements IExplosionSFX {
    public ExplosionEffectWeapon(int cloudCount, float cloudScale, float cloudSpeedMultiplier) {
        super(cloudCount, cloudScale, cloudSpeedMultiplier);
    }
}
