package com.hbm.ntm.explosion.vnt;

import com.hbm.ntm.entity.logic.BalefireExplosionEntity;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorBulkie;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorGlyphidDig;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorWater;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorFire;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorNoDamage;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.CustomDamageHandlerAmat;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCross;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectAmat;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectStandard;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectTiny;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectWeapon;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class WeaponExplosionUtil {
    public static ExplosionVnt standard(Level level, double x, double y, double z, float size, @Nullable Entity source,
            boolean blockDamage, boolean incendiary) {
        return standard(level, x, y, z, size, source, blockDamage, incendiary, false);
    }

    public static ExplosionVnt standard(Level level, double x, double y, double z, float size, @Nullable Entity source,
            boolean blockDamage, boolean incendiary, boolean waterOptimized) {
        ExplosionVnt explosion = new ExplosionVnt(level, x, y, z, size, source, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
        explosion.setBlockAllocator(waterOptimized ? new BlockAllocatorWater(16) : new BlockAllocatorStandard());
        explosion.setBlockProcessor(blockDamage
                ? new BlockProcessorStandard().withBlockEffect(incendiary ? new BlockMutatorFire() : null)
                : new BlockProcessorNoDamage().withBlockEffect(incendiary ? new BlockMutatorFire() : null));
        explosion.setEntityProcessor(new EntityProcessorStandard().allowSelfDamage());
        explosion.setPlayerProcessor(new PlayerProcessorStandard());
        explosion.setEffects(new ExplosionEffectStandard());
        return explosion;
    }

    public static void explodeStandard(Level level, double x, double y, double z, float size, @Nullable Entity source,
            boolean blockDamage, boolean incendiary) {
        standard(level, x, y, z, size, source, blockDamage, incendiary).explode();
    }

    public static ExplosionVnt tiny(Level level, double x, double y, double z, float size, @Nullable Entity source) {
        return new ExplosionVnt(level, x, y, z, size, source, false, Explosion.BlockInteraction.KEEP)
                .setEntityProcessor(new EntityProcessorStandard().allowSelfDamage())
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectTiny());
    }

    public static void explodeTiny(Level level, double x, double y, double z, float size, @Nullable Entity source) {
        tiny(level, x, y, z, size, source).explode();
    }

    public static ExplosionVnt tinySmooth(Level level, double x, double y, double z, float size, @Nullable Entity source,
            float fixedDamage) {
        return tinySmooth(level, x, y, z, size, source, fixedDamage, 0.5D, 0.0F, 0.0F, 0.25D);
    }

    public static ExplosionVnt tinySmooth(Level level, double x, double y, double z, float size, @Nullable Entity source,
            float fixedDamage, double nodeDistance, float pierceDamageThreshold, float pierceDamageResistance, double knockback) {
        EntityProcessorCrossSmooth processor = new EntityProcessorCrossSmooth(nodeDistance, fixedDamage)
                .setupPiercing(pierceDamageThreshold, pierceDamageResistance);
        processor.setKnockback(knockback);
        return new ExplosionVnt(level, x, y, z, size, source, false, Explosion.BlockInteraction.KEEP)
                .setEntityProcessor(processor)
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectTiny());
    }

    public static void explodeTinySmooth(Level level, double x, double y, double z, float size, @Nullable Entity source,
            float fixedDamage) {
        tinySmooth(level, x, y, z, size, source, fixedDamage).explode();
    }

    public static void explodeTinySmooth(Level level, double x, double y, double z, float size, @Nullable Entity source,
            float fixedDamage, double nodeDistance, float pierceDamageThreshold, float pierceDamageResistance, double knockback) {
        tinySmooth(level, x, y, z, size, source, fixedDamage, nodeDistance, pierceDamageThreshold, pierceDamageResistance, knockback).explode();
    }

    public static ExplosionVnt bulkie(Level level, double x, double y, double z, float size, @Nullable Entity source,
            double maximumResistance, int resolution) {
        return new ExplosionVnt(level, x, y, z, size, source, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY)
                .setBlockAllocator(new BlockAllocatorBulkie(maximumResistance, resolution))
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorStandard().allowSelfDamage())
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectStandard());
    }

    public static ExplosionVnt glyphidDig(Level level, double x, double y, double z, float size, @Nullable Entity source,
            double maximumResistance, int resolution) {
        return new ExplosionVnt(level, x, y, z, size, source, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY)
                .setBlockAllocator(new BlockAllocatorGlyphidDig(maximumResistance, resolution))
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorStandard().allowSelfDamage())
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectStandard());
    }

    public static ExplosionVnt smooth(Level level, double x, double y, double z, float size, @Nullable Entity source,
            float fixedDamage, double nodeDistance, boolean blockDamage) {
        return smooth(level, x, y, z, size, source, fixedDamage, nodeDistance, blockDamage, 0.0F, 0.0F);
    }

    public static ExplosionVnt smooth(Level level, double x, double y, double z, float size, @Nullable Entity source,
            float fixedDamage, double nodeDistance, boolean blockDamage, float pierceDamageThreshold, float pierceDamageResistance) {
        ExplosionVnt explosion = new ExplosionVnt(level, x, y, z, size, source, false,
                blockDamage ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.KEEP);
        explosion.setEntityProcessor(new EntityProcessorCrossSmooth(nodeDistance, fixedDamage)
                .setupPiercing(pierceDamageThreshold, pierceDamageResistance));
        explosion.setPlayerProcessor(new PlayerProcessorStandard());
        explosion.setEffects(new ExplosionEffectWeapon(10, 2.5F, 1.0F));
        if (blockDamage) {
            explosion.setBlockAllocator(new BlockAllocatorStandard());
            explosion.setBlockProcessor(new BlockProcessorStandard());
        }
        return explosion;
    }

    public static ExplosionVnt cross(Level level, double x, double y, double z, float size, @Nullable Entity source,
            double nodeDistance, boolean blockDamage) {
        ExplosionVnt explosion = new ExplosionVnt(level, x, y, z, size, source, false,
                blockDamage ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.KEEP);
        explosion.setEntityProcessor(new EntityProcessorCross(nodeDistance).setAllowSelfDamage());
        explosion.setPlayerProcessor(new PlayerProcessorStandard());
        explosion.setEffects(new ExplosionEffectWeapon(10, 2.5F, 1.0F));
        if (blockDamage) {
            explosion.setBlockAllocator(new BlockAllocatorStandard());
            explosion.setBlockProcessor(new BlockProcessorStandard());
        }
        return explosion;
    }

    public static ExplosionVnt antimatter(Level level, double x, double y, double z, float size, @Nullable Entity source,
            float radiation) {
        return new ExplosionVnt(level, x, y, z, size, source, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY)
                .setBlockAllocator(new BlockAllocatorStandard(size < 15.0F ? 16 : 32))
                .setBlockProcessor(new BlockProcessorStandard().setNoDrop())
                .setEntityProcessor(new EntityProcessorStandard().withRangeMod(2.0F).withDamageMod(new CustomDamageHandlerAmat(radiation)))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectAmat());
    }

    public static void spawnBalefire(Level level, double x, double y, double z, int range) {
        if (level == null || level.isClientSide() || range <= 0) {
            return;
        }
        level.addFreshEntity(BalefireExplosionEntity.create(level, x, y, z, range));
        NuclearExplosionUtil.spawnBalefireCloud(level, range, x, y, z);
    }

    private WeaponExplosionUtil() {
    }
}
