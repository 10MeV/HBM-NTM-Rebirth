package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.interfaces.ICustomDamageHandler;
import com.hbm.explosion.vanillant.interfaces.IEntityProcessor;

/** Legacy facade over the modern standard entity processor. */
@Deprecated(forRemoval = false)
public class EntityProcessorStandard extends com.hbm.ntm.explosion.vnt.standard.EntityProcessorStandard implements IEntityProcessor {
    @Override public EntityProcessorStandard withRangeMod(float mod) { super.withRangeMod(mod); return this; }
    public EntityProcessorStandard withDamageMod(ICustomDamageHandler damage) { super.withDamageMod(damage); return this; }
    @Override public EntityProcessorStandard allowSelfDamage() { super.allowSelfDamage(); return this; }
}
