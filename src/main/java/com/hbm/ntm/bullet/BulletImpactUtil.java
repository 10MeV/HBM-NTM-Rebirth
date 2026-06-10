package com.hbm.ntm.bullet;

import com.hbm.ntm.block.ShotDetonatableBlock;
import com.hbm.ntm.damage.DamageClass;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.entity.effect.EmpBlastEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectStandard;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class BulletImpactUtil {
    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock) {
        return applyBlockImpactEffects(config, level, position, source, hitBlock, false);
    }

    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock, boolean inGround) {
        return applyBlockImpactEffects(config, level, position, source, hitBlock, null, inGround);
    }

    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock, @Nullable Direction hitSide, boolean inGround) {
        return applyBlockImpactEffects(config, level, position, source, hitBlock, hitSide, inGround, 0.0F);
    }

    public static BlockImpactResult applyBlockImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable BlockPos hitBlock, @Nullable Direction hitSide, boolean inGround,
            float impactDamage) {
        if (config == null || level == null || position == null) {
            return BlockImpactResult.NONE;
        }

        boolean discard = shouldDiscardAfterBlockImpact(config, hitBlock != null, inGround);
        if (level.isClientSide()) {
            return new BlockImpactResult(discard, false, false, false, false, false, false, false, false, false, false,
                    false, Collections.emptyList());
        }

        boolean fire = applyIncendiaryBlocks(config, level, position);
        fire |= applyFlameBlockImpact(config, level, hitBlock, hitSide);
        boolean extinguish = applyFireExtinguishBlocks(config, level, hitBlock, position);
        boolean emp = applyEmp(config, level, position);
        boolean jolt = applyJolt(config, level, position);
        boolean explosion = applyExplosion(config, level, position, source);
        boolean shrapnel = applyShrapnel(config, level, position, source);
        boolean rainbow = applyRainbow(config, level, position);
        boolean nuke = applyNuke(config, level, position);
        boolean specialBehavior = applyTaggedImpactEffects(config, level, position, source, hitSide,
                impactDamage(config, impactDamage));
        List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests =
                BulletSpecialSpawnUtil.collectImpactSpawnRequests(config, level, source, position, null, hitSide,
                        impactDamage(config, impactDamage));
        BlockBreakResult blockBreak = applyBlockBreak(config, level, hitBlock, source);
        return new BlockImpactResult(discard, fire, emp, jolt, explosion, shrapnel, rainbow, nuke,
                specialBehavior || extinguish,
                blockBreak.destroyedBlock() || blockBreak.brokeGlass() || blockBreak.shotDetonated(),
                blockBreak.shotDetonated(), extinguish, spawnRequests);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position) {
        return applyEntityImpactEffects(config, target, source, position, null);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position, @Nullable RandomSource random) {
        return applyEntityImpactEffects(config, target, source, position, random, 0.0F);
    }

    public static EntityImpactResult applyEntityImpactEffects(BulletConfig config, Entity target,
            @Nullable Entity source, @Nullable Vec3 position, @Nullable RandomSource random, float impactDamage) {
        if (config == null || target == null) {
            return EntityImpactResult.NONE;
        }
        EntityHurtResult hurt = applyEntityHurtEffects(config, target, random);
        Vec3 impact = position == null ? target.position() : position;
        BlockImpactResult block = applyBlockImpactEffects(config, target.level(), impact, source, null, null,
                false, impactDamage);
        return new EntityImpactResult(block.discardProjectile(), hurt, block);
    }

    public static EntityHurtResult applyEntityHurtEffects(BulletConfig config, Entity target) {
        return applyEntityHurtEffects(config, target, null);
    }

    public static EntityHurtResult applyEntityHurtEffects(BulletConfig config, Entity target, @Nullable RandomSource random) {
        if (config == null || target == null || target.level().isClientSide()) {
            return EntityHurtResult.NONE;
        }

        boolean fire = false;
        boolean lead = false;
        boolean instakill = false;
        boolean caustic = false;
        boolean extinguished = false;
        int customEffects = 0;
        if (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_ENTITY)) {
            extinguished = applyEntityExtinguish(target);
        }
        if (config.hasBehavior(BulletBehaviorTag.ENTITY_IGNITE) && target instanceof LivingEntity living) {
            applyEntityIgnite(config, living);
            fire = true;
        }
        if (config.incendiaryTicks() > 0) {
            target.setSecondsOnFire(config.incendiaryTicks());
            fire = true;
        }
        if (config.hasBehavior(BulletBehaviorTag.INCENDIARY_PHOSPHORUS) && target instanceof LivingEntity living) {
            if (HbmLivingProperties.getPhosphorus(living) < 300) {
                HbmLivingProperties.setPhosphorus(living, 300);
            }
            fire = true;
        }
        if (config.hasBehavior(BulletBehaviorTag.INFRARED_BEAM_HIT) && target instanceof LivingEntity living) {
            HbmLivingProperties.ensureFire(living, 100);
            fire = true;
        }
        if (config.hasBehavior(BulletBehaviorTag.BLACK_FIRE_BEAM_HIT) && target instanceof LivingEntity living) {
            HbmLivingProperties.addBlackFire(living, 200);
            fire = true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.LIGHTNING_BEAM_HIT,
                BulletBehaviorTag.LIGHTNING_BEAM_SPLIT) && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 9));
            customEffects += 2;
        }

        RandomSource roll = random == null ? target.level().random : random;
        if (config.leadChance() > 0 && target instanceof LivingEntity living
                && roll.nextInt(100) < config.leadChance()) {
            living.addEffect(new MobEffectInstance(ModEffects.LEAD.get(), 10 * 20, 0));
            lead = true;
        }

        if (config.instakill() && target instanceof LivingEntity living
                && !(target instanceof Player player && player.isCreative())) {
            living.setHealth(0.0F);
            instakill = true;
        }

        if (config.caustic() > 0 && target instanceof Player player) {
            ArmorUtil.damageSuitAll(player, config.caustic());
            caustic = true;
        }

        if (!config.effects().isEmpty() && target instanceof LivingEntity living) {
            for (MobEffectInstance effect : config.effects()) {
                living.addEffect(new MobEffectInstance(effect));
                customEffects++;
            }
        }

        return new EntityHurtResult(fire, lead, instakill, caustic, extinguished, customEffects);
    }

    public static boolean shouldDiscardAfterBlockImpact(BulletConfig config, boolean hasBlockHit, boolean inGround) {
        if (config == null) {
            return false;
        }
        if (hasBlockHit && config.hasBehavior(BulletBehaviorTag.CHARGE_HOOK_STICK)) {
            return false;
        }
        if (hasBlockHit) {
            return !config.liveAfterImpact() && !config.spectral() && !inGround;
        }
        return !config.penetrates();
    }

    public static boolean shouldDiscardAfterEntityImpact(BulletConfig config) {
        return config != null && !config.penetrates();
    }

    private static boolean applyIncendiaryBlocks(BulletConfig config, Level level, Vec3 position) {
        if (config.incendiaryTicks() <= 0) {
            return false;
        }
        BlockPos base = legacyCastPos(position.x, position.y, position.z);
        boolean placed = false;
        placed |= maybeSetFire(level, base);
        placed |= maybeSetFire(level, base.east());
        placed |= maybeSetFire(level, base.west());
        placed |= maybeSetFire(level, base.above());
        placed |= maybeSetFire(level, base.below());
        placed |= maybeSetFire(level, base.south());
        placed |= maybeSetFire(level, base.north());
        return placed;
    }

    private static boolean maybeSetFire(Level level, BlockPos pos) {
        if (level.random.nextInt(3) != 0 || !level.getBlockState(pos).isAir()) {
            return false;
        }
        return level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
    }

    private static boolean applyFlameBlockImpact(BulletConfig config, Level level, @Nullable BlockPos hitBlock,
            @Nullable Direction hitSide) {
        if (hitBlock == null || hitSide == null || !hasAnyBehavior(config,
                BulletBehaviorTag.FLAME_LINGER_DIESEL,
                BulletBehaviorTag.FLAME_LINGER_GAS,
                BulletBehaviorTag.FLAME_LINGER_NAPALM,
                BulletBehaviorTag.FLAME_LINGER_BALEFIRE,
                BulletBehaviorTag.INFRARED_BEAM_HIT)) {
            return false;
        }
        BlockState hitState = level.getBlockState(hitBlock);
        if (!hitState.isFlammable(level, hitBlock, hitSide)) {
            return false;
        }
        BlockPos firePos = hitBlock.relative(hitSide);
        if (!level.getBlockState(firePos).isAir()) {
            return false;
        }
        BlockState fireState = config.hasBehavior(BulletBehaviorTag.FLAME_LINGER_BALEFIRE)
                ? ModBlocks.BALEFIRE.get().defaultBlockState()
                : BaseFireBlock.getState(level, firePos);
        if (!fireState.canSurvive(level, firePos)) {
            return false;
        }
        boolean placed = level.setBlock(firePos, fireState, 3);
        if (placed) {
            level.playSound(null, firePos, ModSounds.WEAPON_FLAMETHROWER_IGNITE.get(),
                    SoundSource.BLOCKS, 0.75F, 0.9F + level.random.nextFloat() * 0.2F);
        }
        return placed;
    }

    private static boolean applyEntityExtinguish(Entity target) {
        boolean changed = target.getRemainingFireTicks() > 0;
        target.clearFire();
        if (target instanceof LivingEntity living) {
            changed |= HbmLivingProperties.getFire(living) > 0
                    || HbmLivingProperties.getPhosphorus(living) > 0
                    || HbmLivingProperties.getBalefire(living) > 0
                    || HbmLivingProperties.getBlackFire(living) > 0;
            HbmLivingProperties.setFire(living, 0);
            HbmLivingProperties.setPhosphorus(living, 0);
            HbmLivingProperties.setBalefire(living, 0);
            HbmLivingProperties.setBlackFire(living, 0);
        }
        return changed;
    }

    private static void applyEntityIgnite(BulletConfig config, LivingEntity target) {
        if (config.hasBehavior(BulletBehaviorTag.BALEFIRE_VISUAL)
                || config.hasBehavior(BulletBehaviorTag.FLAME_LINGER_BALEFIRE)) {
            HbmLivingProperties.ensureBalefire(target, 200);
            return;
        }
        HbmLivingProperties.ensureFire(target, hasAnyBehavior(config,
                BulletBehaviorTag.FLAME_LINGER_DIESEL,
                BulletBehaviorTag.FLAME_LINGER_GAS,
                BulletBehaviorTag.FLAME_LINGER_NAPALM) ? 100 : 200);
    }

    private static boolean applyFireExtinguishBlocks(BulletConfig config, Level level, @Nullable BlockPos hitBlock,
            Vec3 position) {
        if (!hasAnyBehavior(config, BulletBehaviorTag.FIRE_EXTINGUISH_WATER,
                BulletBehaviorTag.FIRE_EXTINGUISH_FOAM, BulletBehaviorTag.FIRE_EXTINGUISH_SAND)) {
            return false;
        }
        BlockPos center = hitBlock == null ? legacyFloorPos(position.x, position.y, position.z) : hitBlock;
        boolean changed = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockState state = level.getBlockState(pos);
            if (isExtinguishableFire(state)
                    || (config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_WATER) && isLegacyFoam(state))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                changed = true;
            }
        }
        if (changed) {
            level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    1.0F, 1.5F + level.random.nextFloat() * 0.5F);
        }
        return changed;
    }

    private static boolean isExtinguishableFire(BlockState state) {
        return state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(ModBlocks.FIRE_DIGAMMA.get())
                || state.is(ModBlocks.BALEFIRE.get());
    }

    private static boolean isLegacyFoam(BlockState state) {
        return "block_foam".equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
    }

    private static boolean applyEmp(BulletConfig config, Level level, Vec3 position) {
        if (config.emp() <= 0) {
            return false;
        }
        ExplosionNukeGeneric.empBlast(level, legacyRoundPos(position.x), legacyRoundPos(position.y),
                legacyRoundPos(position.z), config.emp());
        if (config.emp() > 3) {
            level.addFreshEntity(EmpBlastEntity.create(level, position.x, position.y + 0.5D, position.z,
                    config.emp()));
        }
        return true;
    }

    private static boolean applyJolt(BulletConfig config, Level level, Vec3 position) {
        if (config.jolt() <= 0.0D) {
            return false;
        }
        ExplosionLarge.jolt(level, position.x, position.y, position.z, config.jolt(), 150, 0.25D);
        return true;
    }

    private static boolean applyExplosion(BulletConfig config, Level level, Vec3 position, @Nullable Entity source) {
        if (config.explosive() <= 0.0F) {
            return false;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.TINY_EXPLODE,
                BulletBehaviorTag.GRENADE_TINY_EXPLOSIVE_PELLET)) {
            WeaponExplosionUtil.explodeTinySmooth(level, position.x, position.y, position.z,
                    config.explosive(), source, fixedExplosionDamage(config), 0.5D,
                    config.armorThresholdNegation(), config.armorPiercingPercent(), 0.25D);
            return true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.DEMO_EXPLODE,
                BulletBehaviorTag.TURRET_240_VNT_EXPLODE,
                BulletBehaviorTag.CHARGE_MORTAR_EXPLODE,
                BulletBehaviorTag.CHARGE_MORTAR_CHARGE_EXPLODE,
                BulletBehaviorTag.MINI_NUKE_DEMO,
                BulletBehaviorTag.MINI_NUKE_BALEFIRE)) {
            WeaponExplosionUtil.smooth(level, position.x, position.y, position.z,
                    config.explosive(), source, fixedExplosionDamage(config), 1.0D, true,
                    config.armorThresholdNegation(), config.armorPiercingPercent()).explode();
            return true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.STANDARD_EXPLODE,
                BulletBehaviorTag.HEAT_EXPLODE,
                BulletBehaviorTag.INCENDIARY_EXPLODE,
                BulletBehaviorTag.PHOSPHORUS_EXPLODE,
                BulletBehaviorTag.TURRET_240_STANDARD_EXPLODE,
                BulletBehaviorTag.MINI_NUKE_STANDARD,
                BulletBehaviorTag.MINI_NUKE_TINYTOT,
                BulletBehaviorTag.MINI_NUKE_HIVE)) {
            WeaponExplosionUtil.smooth(level, position.x, position.y, position.z,
                    config.explosive(), source, fixedExplosionDamage(config), 1.0D, false,
                    config.armorThresholdNegation(), config.armorPiercingPercent()).explode();
            return true;
        }
        WeaponExplosionUtil.explodeStandard(level, position.x, position.y, position.z, config.explosive(), source,
                config.blockDamage(), config.incendiaryTicks() > 0);
        return true;
    }

    private static float fixedExplosionDamage(BulletConfig config) {
        return Math.max(1.0F, config.damageMax());
    }

    private static boolean hasAnyBehavior(BulletConfig config, BulletBehaviorTag... behaviors) {
        for (BulletBehaviorTag behavior : behaviors) {
            if (config.hasBehavior(behavior)) {
                return true;
            }
        }
        return false;
    }

    private static boolean applyShrapnel(BulletConfig config, Level level, Vec3 position, @Nullable Entity source) {
        if (config.shrapnel() <= 0) {
            return false;
        }
        ExplosionLarge.spawnShrapnels(level, position.x, position.y, position.z, config.shrapnel(), 1.0F, source);
        return true;
    }

    private static boolean applyRainbow(BulletConfig config, Level level, Vec3 position) {
        if (config.rainbow() <= 0) {
            return false;
        }
        boolean spawned = NuclearExplosionUtil.spawnFleijaRainbow(level, position.x, position.y, position.z,
                config.rainbow(), config.rainbow());
        if (spawned) {
            level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS, 100.0F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        return spawned;
    }

    private static boolean applyNuke(BulletConfig config, Level level, Vec3 position) {
        if (config.nuke() <= 0) {
            return false;
        }
        boolean spawned = level.addFreshEntity(NukeExplosionMk5Entity.statFac(level, config.nuke(),
                position.x, position.y, position.z));
        if (spawned) {
            ParticleUtil.spawnMuke(level, position.x, position.y + 0.5D, position.z,
                    level.random.nextInt(100) == 0);
            level.playSound(null, position.x, position.y, position.z, ModSounds.WEAPON_MUKE_EXPLOSION.get(),
                    SoundSource.BLOCKS, 15.0F, 1.0F);
        }
        return spawned;
    }

    private static boolean applyTaggedImpactEffects(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, @Nullable Direction hitSide, float impactDamage) {
        if (config.hasBehavior(BulletBehaviorTag.UFO_BLAST)) {
            applyUfoBlast(level, position);
            return true;
        }
        if (hasAnyBehavior(config, BulletBehaviorTag.LIGHTNING_BEAM_HIT,
                BulletBehaviorTag.LIGHTNING_BEAM_SPLIT)) {
            applyLightningBeamHit(level, offsetBeamBlockImpact(position, hitSide), source, impactDamage);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM)) {
            applyBatterySocketDischarge(level, position, source);
            return true;
        }
        if (config.hasBehavior(BulletBehaviorTag.SHREDDER_BEAM_SPLIT)) {
            applyShredderPulse(level, position, hitSide);
            if (hitSide != null) {
                applyShredderBeamBlockDamage(config, level, position, source, impactDamage);
            }
            return true;
        }
        return false;
    }

    private static Vec3 offsetBeamBlockImpact(Vec3 position, @Nullable Direction hitSide) {
        return hitSide == null ? position : position.add(hitSide.getStepX() * 0.5D,
                hitSide.getStepY() * 0.5D, hitSide.getStepZ() * 0.5D);
    }

    private static void applyLightningBeamHit(Level level, Vec3 position, @Nullable Entity source,
            float impactDamage) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        new ExplosionVnt(level, position.x, position.y, position.z, 2.0F, source, false,
                Explosion.BlockInteraction.KEEP)
                .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, impactDamage)
                        .setDamageClass(DamageClass.ELECTRIC))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .explode();
        level.playSound(null, position.x, position.y, position.z, ModSounds.ENTITY_UFO_BLAST.get(),
                SoundSource.BLOCKS, 5.0F, 0.9F + level.random.nextFloat() * 0.2F);
        level.playSound(null, position.x, position.y, position.z, SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.BLOCKS, 5.0F, 0.5F);

        float yaw = level.random.nextFloat() * 180.0F;
        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnPlasmaBlast(level, position.x, position.y, position.z,
                    0.5F, 0.5F, 1.0F, -60.0F + 60.0F * i, yaw, 2.0F);
        }
    }

    private static void applyBatterySocketDischarge(Level level, Vec3 position, @Nullable Entity source) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        new ExplosionVnt(level, position.x, position.y, position.z, 5.0F, source, false,
                Explosion.BlockInteraction.KEEP)
                .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, 20.0F)
                        .setDamageClass(DamageClass.ELECTRIC))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectStandard())
                .explode();
        level.playSound(null, position.x, position.y, position.z, ModSounds.ENTITY_UFO_BLAST.get(),
                SoundSource.BLOCKS, 5.0F, 0.9F + level.random.nextFloat() * 0.2F);
    }

    public static void applyShredderPulse(Level level, Vec3 position, @Nullable Direction hitSide) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        Vec3 pulse = hitSide == null ? position : position.add(hitSide.getStepX() * 0.05D,
                hitSide.getStepY() * 0.05D, hitSide.getStepZ() * 0.05D);
        float yaw = 0.0F;
        float pitch = 0.0F;
        if (hitSide != null) {
            switch (hitSide) {
                case NORTH -> pitch = 90.0F;
                case SOUTH -> {
                    yaw = 180.0F;
                    pitch = 90.0F;
                }
                case EAST -> {
                    yaw = 90.0F;
                    pitch = 90.0F;
                }
                case WEST -> {
                    yaw = 270.0F;
                    pitch = 90.0F;
                }
                default -> {
                }
            }
        }
        ParticleUtil.spawnPlasmaBlast(level, pulse.x, pulse.y, pulse.z, 0.5F, 0.5F, 1.0F, pitch, yaw, 0.75F);
    }

    public static int applyShredderPulseDamage(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, float impactDamage, double radius) {
        if (config == null || level == null || level.isClientSide() || position == null || impactDamage <= 0.0F) {
            return 0;
        }
        AABB area = new AABB(position, position).inflate(radius);
        int damaged = 0;
        for (Entity entity : level.getEntities((Entity) null, area, Entity::isAlive)) {
            if (EntityDamageUtil.attackEntityFromNt(entity, config.damageSource(level, null, source), impactDamage,
                    true, false, 0.0D, 0.0F, 0.0F)) {
                damaged++;
            }
        }
        return damaged;
    }

    private static int applyShredderBeamBlockDamage(BulletConfig config, Level level, Vec3 position,
            @Nullable Entity source, float impactDamage) {
        return applyShredderPulseDamage(config, level, position, source, impactDamage, 0.75D);
    }

    public static void applyUfoBlast(Level level, Vec3 position) {
        if (level == null || level.isClientSide() || position == null) {
            return;
        }
        level.playSound(null, position.x, position.y, position.z, ModSounds.ENTITY_UFO_BLAST.get(),
                SoundSource.BLOCKS, 5.0F, 0.9F + level.random.nextFloat() * 0.2F);
        level.playSound(null, position.x, position.y, position.z, SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.BLOCKS, 5.0F, 0.5F);
        ExplosionNukeGeneric.dealDamage(level, position.x, position.y, position.z, 10.0D, 50.0F);

        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnPlasmaBlast(level, position.x, position.y, position.z,
                    0.0F, 0.75F, 1.0F, -30.0F + 30.0F * i, level.random.nextFloat() * 180.0F, 5.0F);
        }
    }

    private static BlockBreakResult applyBlockBreak(BulletConfig config, Level level,
            @Nullable BlockPos hitBlock, @Nullable Entity source) {
        if (hitBlock == null) {
            return BlockBreakResult.NONE;
        }
        BlockState state = level.getBlockState(hitBlock);
        if (state.isAir()) {
            return BlockBreakResult.NONE;
        }
        if (config.hasBehavior(BulletBehaviorTag.BATTERY_SOCKET_DISCHARGE_BEAM)) {
            return new BlockBreakResult(level.destroyBlock(hitBlock, false), false, false);
        }
        if (config.destroysBlocks() && state.getDestroySpeed(level, hitBlock) <= 120.0F) {
            return new BlockBreakResult(level.destroyBlock(hitBlock, false), false, false);
        }
        if (config.breaksGlass() && isLegacyGlass(state)) {
            return new BlockBreakResult(false, level.destroyBlock(hitBlock, false), false);
        }
        if (config.breaksGlass() && state.getBlock() instanceof ShotDetonatableBlock detonatable) {
            return new BlockBreakResult(false, false,
                    detonatable.detonateFromShot(level, hitBlock, state, source));
        }
        return BlockBreakResult.NONE;
    }

    private static boolean isLegacyGlass(BlockState state) {
        Block block = state.getBlock();
        return state.is(Blocks.GLASS)
                || state.is(Blocks.GLASS_PANE)
                || block instanceof StainedGlassBlock
                || block instanceof StainedGlassPaneBlock;
    }

    private static BlockPos legacyCastPos(double x, double y, double z) {
        return new BlockPos((int) x, (int) y, (int) z);
    }

    private static BlockPos legacyFloorPos(double x, double y, double z) {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    private static int legacyRoundPos(double value) {
        return (int) (value + 0.5D);
    }

    private static float impactDamage(BulletConfig config, float impactDamage) {
        return impactDamage > 0.0F ? impactDamage : fixedExplosionDamage(config);
    }

    public record BlockImpactResult(boolean discardProjectile, boolean placedFire, boolean emp, boolean jolt,
            boolean explosion, boolean shrapnel, boolean rainbow, boolean nuke, boolean specialBehavior,
            boolean brokeOrDestroyedBlock, boolean shotDetonated,
            boolean extinguishedFire,
            List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests) {
        public static final BlockImpactResult NONE = new BlockImpactResult(false, false, false, false, false, false,
                false, false, false, false, false, false, Collections.emptyList());
    }

    public record EntityImpactResult(boolean discardProjectile, EntityHurtResult hurt, BlockImpactResult blockImpact) {
        public static final EntityImpactResult NONE = new EntityImpactResult(false, EntityHurtResult.NONE,
                BlockImpactResult.NONE);
    }

    public record EntityHurtResult(boolean setOnFire, boolean leadApplied, boolean instakilled,
            boolean damagedArmor, boolean extinguishedFire, int customEffectsApplied) {
        public static final EntityHurtResult NONE = new EntityHurtResult(false, false, false, false, false, 0);
    }

    private record BlockBreakResult(boolean destroyedBlock, boolean brokeGlass, boolean shotDetonated) {
        private static final BlockBreakResult NONE = new BlockBreakResult(false, false, false);
    }

    private BulletImpactUtil() {
    }
}
