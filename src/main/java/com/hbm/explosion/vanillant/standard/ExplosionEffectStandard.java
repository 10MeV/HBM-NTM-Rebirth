package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;

/** Legacy facade over the modern standard explosion effect. */
@Deprecated(forRemoval = false)
public class ExplosionEffectStandard extends com.hbm.ntm.explosion.vnt.standard.ExplosionEffectStandard implements IExplosionSFX {
    public ExplosionEffectStandard() { super(); }
    public ExplosionEffectStandard(boolean sound, boolean particles) { super(sound, particles); }
}
