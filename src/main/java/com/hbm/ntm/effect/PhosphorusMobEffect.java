package com.hbm.ntm.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PhosphorusMobEffect extends MobEffect {
    public PhosphorusMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFF00);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            entity.setSecondsOnFire(1);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
