package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.ICustomDamageHandler;
import com.hbm.explosion.vanillant.interfaces.IEntityProcessor;

/** Legacy facade over the modern cross entity processor. */
@Deprecated(forRemoval = false)
public class EntityProcessorCross extends com.hbm.ntm.explosion.vnt.standard.EntityProcessorCross implements IEntityProcessor {
    public EntityProcessorCross() { super(); }
    public EntityProcessorCross(double nodeDistance) { super(nodeDistance); }
    @Override public EntityProcessorCross withRangeMod(float mod) { super.withRangeMod(mod); return this; }
    public EntityProcessorCross withDamageMod(ICustomDamageHandler damage) { super.withDamageMod(damage); return this; }
    @Override public EntityProcessorCross setAllowSelfDamage() { super.setAllowSelfDamage(); return this; }
    @Override public EntityProcessorCross setKnockback(double multiplier) { super.setKnockback(multiplier); return this; }
}
