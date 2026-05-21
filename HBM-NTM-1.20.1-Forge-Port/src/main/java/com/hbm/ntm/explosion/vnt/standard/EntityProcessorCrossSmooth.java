package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.world.entity.Entity;

public class EntityProcessorCrossSmooth extends EntityProcessorCross {
    private final float fixedDamage;
    private float pierceDamageThreshold;
    private float pierceDamageResistance;

    public EntityProcessorCrossSmooth(double nodeDistance, float fixedDamage) {
        super(nodeDistance);
        this.fixedDamage = fixedDamage;
        setAllowSelfDamage();
    }

    public EntityProcessorCrossSmooth setupPiercing(float pierceDamageThreshold, float pierceDamageResistance) {
        this.pierceDamageThreshold = pierceDamageThreshold;
        this.pierceDamageResistance = pierceDamageResistance;
        return this;
    }

    @Override
    protected void attackEntity(Entity entity, ExplosionVnt explosion, float amount) {
        if (!entity.isAlive()) {
            return;
        }
        if (explosion.exploder() == entity) {
            amount *= 0.5F;
        }
        entity.hurt(explosion.damageSource(), amount);
    }

    @Override
    public float calculateDamage(double distanceScaled, double density, double knockback, float diameter) {
        if (density < 0.125D) {
            return 0.0F;
        }
        return (float) (fixedDamage * (1.0D - distanceScaled));
    }

    public float pierceDamageThreshold() {
        return pierceDamageThreshold;
    }

    public float pierceDamageResistance() {
        return pierceDamageResistance;
    }
}
