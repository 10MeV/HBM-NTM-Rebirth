package com.hbm.ntm.effect;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class LeadMobEffect extends MobEffect {
    public LeadMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x767682);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.source(entity.level(), ModDamageSources.LEAD), amplifier + 1.0F);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 60 == 0;
    }
}
