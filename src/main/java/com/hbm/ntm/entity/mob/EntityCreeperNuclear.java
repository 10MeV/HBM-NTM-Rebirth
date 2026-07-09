package com.hbm.ntm.entity.mob;

import com.hbm.ntm.api.entity.IRadiationImmune;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.ExplosionNukeSmall;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityCreeperNuclear extends Creeper implements IRadiationImmune {
    private static final int LEGACY_FUSE_TIME = 75;
    private static final double CONTAMINATION_RADIUS = 5.0D;

    public EntityCreeperNuclear(EntityType<? extends EntityCreeperNuclear> type, Level level) {
        super(type, level);
        this.maxSwell = LEGACY_FUSE_TIME;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isRemoved() || isDeadOrDying()) {
            return false;
        }
        if (source.is(ModDamageSources.RADIATION) || source.is(ModDamageSources.MUD_POISONING)) {
            if (isAlive()) {
                heal(amount);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(CONTAMINATION_RADIUS), living -> living != this);
            for (LivingEntity living : nearby) {
                RadiationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 0.25F);
            }
        }

        super.tick();

        if (!level().isClientSide() && isAlive() && getHealth() < getMaxHealth() && tickCount % 10 == 0) {
            heal(1.0F);
        }
    }

    @Override
    protected void explodeCreeper() {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }

        discard();
        boolean mobGriefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        double x = getX();
        double y = getY();
        double z = getZ();
        double effectY = y + 0.5D;

        if (isPowered()) {
            ParticleUtil.spawnMuke(level, x, effectY, z, false);
            LegacySoundPlayer.playLegacyMukeExplosion(level, x, effectY, z);
            if (mobGriefing) {
                NuclearExplosionUtil.spawnNuclearCoreLoaded(level, 50, x, y, z);
            } else {
                ExplosionNukeGeneric.dealDamage(level, x, effectY, z, 100.0D);
            }
            return;
        }

        ExplosionNukeSmall.explode(level, x, effectY, z,
                mobGriefing ? ExplosionNukeSmall.PARAMS_MEDIUM : ExplosionNukeSmall.PARAMS_SAFE);
    }
}
