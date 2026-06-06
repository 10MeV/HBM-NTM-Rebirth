package com.hbm.ntm.explosion.vnt.interfaces;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface CustomDamageHandler {
    void handleAttack(ExplosionVnt explosion, Entity entity, double distanceScaled);
}
