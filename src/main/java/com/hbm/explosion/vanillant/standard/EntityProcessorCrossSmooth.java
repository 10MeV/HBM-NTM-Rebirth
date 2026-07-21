package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.ICustomDamageHandler;
import com.hbm.explosion.vanillant.interfaces.IEntityProcessor;
import com.hbm.ntm.damage.DamageClass;

/** Legacy facade over the modern smooth-cross entity processor. */
@Deprecated(forRemoval = false)
public class EntityProcessorCrossSmooth extends com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth implements IEntityProcessor {
    public EntityProcessorCrossSmooth(double nodeDistance, float fixedDamage) { super(nodeDistance, fixedDamage); }
    @Override public EntityProcessorCrossSmooth setupPiercing(float threshold, float resistance) { super.setupPiercing(threshold, resistance); return this; }
    @Override public EntityProcessorCrossSmooth setDamageClass(DamageClass damageClass) { super.setDamageClass(damageClass); return this; }
    @Override public EntityProcessorCrossSmooth withRangeMod(float mod) { super.withRangeMod(mod); return this; }
    public EntityProcessorCrossSmooth withDamageMod(ICustomDamageHandler damage) { super.withDamageMod(damage); return this; }
    @Override public EntityProcessorCrossSmooth setAllowSelfDamage() { super.setAllowSelfDamage(); return this; }
    @Override public EntityProcessorCrossSmooth setKnockback(double multiplier) { super.setKnockback(multiplier); return this; }
}
