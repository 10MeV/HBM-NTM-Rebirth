package com.hbm.ntm.effect;

import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class RadiationMobEffect extends MobEffect {
    public RadiationMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x84C128);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        RadiationUtil.applyRadiationEffect(entity, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
