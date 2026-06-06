package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.CustomDamageHandler;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class CustomDamageHandlerAmat implements CustomDamageHandler {
    private final float radiation;

    public CustomDamageHandlerAmat(float radiation) {
        this.radiation = radiation;
    }

    @Override
    public void handleAttack(ExplosionVnt explosion, Entity entity, double distanceScaled) {
        if (entity instanceof LivingEntity livingEntity) {
            RadiationUtil.contaminate(livingEntity, (float) (radiation * (1.0D - distanceScaled) * explosion.size()), false);
        }
    }
}
