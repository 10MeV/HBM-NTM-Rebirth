package com.hbm.ntm.effect;

import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class RadawayMobEffect extends MobEffect {
    public RadawayMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xBB4B00);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        RadiationUtil.applyRadaway(entity, amplifier + 1.0F);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
