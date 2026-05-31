package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.damage.DamageClass;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityProcessorCrossSmooth extends EntityProcessorCross {
    private final float fixedDamage;
    private float pierceDamageThreshold;
    private float pierceDamageResistance;
    private DamageClass damageClass = DamageClass.EXPLOSIVE;

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

    public EntityProcessorCrossSmooth setDamageClass(DamageClass damageClass) {
        this.damageClass = damageClass == null ? DamageClass.EXPLOSIVE : damageClass;
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
        EntityDamageUtil.attackEntityFromNt(entity, damageSource(explosion), amount, true, false, 0.0D,
                pierceDamageThreshold, pierceDamageResistance);
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

    public DamageClass damageClass() {
        return damageClass;
    }

    private DamageSource damageSource(ExplosionVnt explosion) {
        Level level = explosion.level();
        return switch (damageClass) {
            case PHYSICAL -> ModDamageSources.source(level, ModDamageSources.REVOLVER_BULLET, explosion.exploder());
            case FIRE -> level.damageSources().onFire();
            case ELECTRIC -> ModDamageSources.electric(level);
            case PLASMA -> ModDamageSources.source(level, ModDamageSources.PLASMA, explosion.exploder());
            case LASER -> ModDamageSources.source(level, ModDamageSources.LASER, explosion.exploder());
            case MICROWAVE -> ModDamageSources.source(level, ModDamageSources.MICROWAVE, explosion.exploder());
            case SUBATOMIC -> ModDamageSources.source(level, ModDamageSources.SUBATOMIC, explosion.exploder());
            case OTHER -> explosion.damageSource();
            case EXPLOSIVE -> explosion.damageSource();
        };
    }
}
