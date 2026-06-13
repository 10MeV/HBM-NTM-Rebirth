package com.hbm.ntm.bullet;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulletSpecialSpawnUtil {
    public static final int MASKMAN_ORB_VOLLEY_PERIOD = 10;
    public static final int MASKMAN_ORB_VOLLEY_PHASE = 5;
    public static final double MASKMAN_ORB_VOLLEY_RANGE = 50.0D;
    public static final float MASKMAN_BOLT_THROW_FORCE = 0.5F;
    public static final float MASKMAN_BOLT_DEVIATION = 0.05F;
    public static final int MASKMAN_METEOR_MIN_Y_OFFSET = 30;
    public static final int MASKMAN_METEOR_EXTRA_Y_RANGE = 10;
    public static final Vec3 MASKMAN_METEOR_MOTION = new Vec3(0.0D, -1.0D, 0.0D);
    public static final double LEGACY_GRENADE_PROJECTILE_Y_OFFSET = 0.05D;
    public static final double LEGACY_GRENADE_LASER_Y_OFFSET = 0.125D;
    public static final float LEGACY_GRENADE_FRAGMENT_DAMAGE = 10.0F;
    public static final float LEGACY_GRENADE_PELLET_DAMAGE = 15.0F;
    public static final float LEGACY_GRENADE_HEAVY_PELLET_DAMAGE = 30.0F;
    public static final float LEGACY_GRENADE_LASER_DAMAGE = 30.0F;
    public static final double LEGACY_GRENADE_LASER_RANGE = 15.0D;
    public static final double LIGHTNING_SPLIT_RANGE = 20.0D;
    public static final float SHREDDER_SUBMUNITION_GUN_SPREAD = 0.2F;
    public static final double SHREDDER_BLOCK_SIDE_OFFSET = 0.1D;

    public static List<SpawnRequest> collectPreMoveSpawnRequests(BulletConfig config, @Nullable Entity projectile,
            @Nullable Entity shooter, int ticksExisted, @Nullable RandomSource random) {
        if (config == null || projectile == null || projectile.level().isClientSide()) {
            return Collections.emptyList();
        }
        if (!config.hasBehavior(BulletBehaviorTag.MASKMAN_ORB_BOLT_VOLLEY)
                || ticksExisted % MASKMAN_ORB_VOLLEY_PERIOD != MASKMAN_ORB_VOLLEY_PHASE) {
            return Collections.emptyList();
        }

        Level level = projectile.level();
        RandomSource roll = random == null ? level.random : random;
        AABB search = projectile.getBoundingBox().inflate(MASKMAN_ORB_VOLLEY_RANGE);
        List<Player> players = level.getEntitiesOfClass(Player.class, search, Player::isAlive);
        if (players.isEmpty()) {
            return Collections.emptyList();
        }

        List<SpawnRequest> requests = new ArrayList<>(players.size());
        Vec3 origin = projectile.position();
        for (Player player : players) {
            Vec3 heading = new Vec3(player.getX() - origin.x, player.getY() + player.getEyeHeight() - origin.y,
                    player.getZ() - origin.z);
            if (heading.lengthSqr() <= 1.0E-7D) {
                continue;
            }
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(LegacyBulletConfigs.MASKMAN_BOLT,
                    origin, heading.normalize(), MASKMAN_BOLT_THROW_FORCE, MASKMAN_BOLT_DEVIATION, roll);
            requests.add(new SpawnRequest(SpawnType.MASKMAN_ORB_BOLT, LegacyBulletConfigs.MASKMAN_BOLT, plan,
                    shooter, player, origin));
        }
        return Collections.unmodifiableList(requests);
    }

    public static List<SpawnRequest> collectLegacyGrenadeFragmentation(Level level, @Nullable Entity thrower,
            Vec3 position, float fragments, boolean fragShell, @Nullable RandomSource random) {
        if (level == null || position == null || level.isClientSide()) {
            return Collections.emptyList();
        }
        float count = fragShell ? fragments * 1.5F : fragments;
        int fragmentCount = Math.max(0, (int) Math.ceil(count));
        if (fragmentCount <= 0) {
            return Collections.emptyList();
        }
        RandomSource roll = random == null ? level.random : random;
        Vec3 origin = position.add(0.0D, LEGACY_GRENADE_PROJECTILE_Y_OFFSET, 0.0D);
        List<SpawnRequest> requests = new ArrayList<>(fragmentCount);
        for (int i = 0; i < fragmentCount; i++) {
            float yaw = roll.nextFloat() * Mth.PI * 2.0F;
            float pitch = (roll.nextFloat() - 0.5F) * 2.0F * Mth.PI;
            BulletLaunchUtil.LaunchPlan plan = legacyYawPitchLaunchPlan(
                    LegacySednaRuntimeBulletConfigs.GRENADE_FRAGMENTATION, origin, yaw, pitch, roll);
            requests.add(new SpawnRequest(SpawnType.GRENADE_FRAGMENTATION,
                    LegacySednaRuntimeBulletConfigs.GRENADE_FRAGMENTATION, plan, thrower, null, position,
                    LEGACY_GRENADE_FRAGMENT_DAMAGE));
        }
        return Collections.unmodifiableList(requests);
    }

    public static List<SpawnRequest> collectLegacyGrenadeClusterPellets(Level level, @Nullable Entity thrower,
            Vec3 position, boolean heavy, boolean fragShell, @Nullable RandomSource random) {
        if (level == null || position == null || level.isClientSide()) {
            return Collections.emptyList();
        }
        float count = heavy ? 15.0F : 30.0F;
        if (!heavy && fragShell) {
            count *= 1.25F;
        }
        int pelletCount = Math.max(0, (int) Math.ceil(count));
        if (pelletCount <= 0) {
            return Collections.emptyList();
        }
        RandomSource roll = random == null ? level.random : random;
        Vec3 origin = position.add(0.0D, LEGACY_GRENADE_PROJECTILE_Y_OFFSET, 0.0D);
        BulletConfig pelletConfig = heavy ? LegacySednaRuntimeBulletConfigs.GRENADE_PELLETS_HEAVY
                : LegacySednaRuntimeBulletConfigs.GRENADE_PELLETS;
        SpawnType spawnType = heavy ? SpawnType.GRENADE_CLUSTER_HEAVY_PELLET : SpawnType.GRENADE_CLUSTER_PELLET;
        float damage = heavy ? LEGACY_GRENADE_HEAVY_PELLET_DAMAGE : LEGACY_GRENADE_PELLET_DAMAGE;
        double yScale = heavy ? 1.25D : 0.75D;
        List<SpawnRequest> requests = new ArrayList<>(pelletCount);
        for (int i = 0; i < pelletCount; i++) {
            float yaw = roll.nextFloat() * Mth.PI * 2.0F;
            float pitch = (roll.nextFloat() * 0.5F + 0.5F) * Mth.PI;
            BulletLaunchUtil.LaunchPlan plan = legacyYawPitchLaunchPlan(pelletConfig, origin, yaw, pitch, roll);
            plan = BulletLaunchUtil.withMotion(plan,
                    new Vec3(plan.motion().x * 0.5D, plan.motion().y * yScale, plan.motion().z * 0.5D));
            requests.add(new SpawnRequest(spawnType, pelletConfig, plan, thrower, null, position, damage));
        }
        return Collections.unmodifiableList(requests);
    }

    public static List<SpawnRequest> collectLegacyGrenadeLaserBeams(Level level, @Nullable Entity thrower,
            Vec3 position, @Nullable RandomSource random) {
        if (level == null || position == null || level.isClientSide()) {
            return Collections.emptyList();
        }
        RandomSource roll = random == null ? level.random : random;
        Vec3 origin = position.add(0.0D, LEGACY_GRENADE_LASER_Y_OFFSET, 0.0D);
        AABB area = new AABB(origin, origin).inflate(LEGACY_GRENADE_LASER_RANGE);
        List<LivingEntity> potentialTargets = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, area,
                LivingEntity::isAlive));
        Collections.shuffle(potentialTargets);
        List<SpawnRequest> requests = new ArrayList<>(potentialTargets.size());
        for (LivingEntity target : potentialTargets) {
            if (target == thrower) {
                continue;
            }
            Vec3 delta = new Vec3(target.getX() - origin.x,
                    target.getY() + target.getBbHeight() / 2.0D - origin.y,
                    target.getZ() - origin.z);
            if (delta.length() > LEGACY_GRENADE_LASER_RANGE || delta.lengthSqr() <= 1.0E-7D) {
                continue;
            }
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(
                    LegacySednaRuntimeBulletConfigs.GRENADE_LASER, origin, delta.normalize(), 1.0F, 0.0F, roll);
            requests.add(new SpawnRequest(SpawnType.GRENADE_LASER_BEAM,
                    LegacySednaRuntimeBulletConfigs.GRENADE_LASER, plan, thrower, target, position,
                    LEGACY_GRENADE_LASER_DAMAGE));
        }
        return Collections.unmodifiableList(requests);
    }

    public static List<SpawnRequest> collectLightningSplitSubBeams(BulletConfig config, Level level,
            @Nullable Entity shooter, Entity hitEntity, Vec3 impactPosition, @Nullable RandomSource random,
            float impactDamage) {
        if (config == null || level == null || impactPosition == null || hitEntity == null
                || level.isClientSide() || !config.hasBehavior(BulletBehaviorTag.LIGHTNING_BEAM_SPLIT)) {
            return Collections.emptyList();
        }
        RandomSource roll = random == null ? level.random : random;
        AABB area = new AABB(impactPosition, impactPosition).inflate(LIGHTNING_SPLIT_RANGE);
        List<LivingEntity> potentialTargets = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, area,
                LivingEntity::isAlive));
        Collections.shuffle(potentialTargets);

        BulletConfig subBeam = LegacySednaRuntimeBulletConfigs.ENERGY_TESLA_IR_SUB;
        float damage = impactDamage > 0.0F ? impactDamage * subBeam.damageMax() : 0.0F;
        List<SpawnRequest> requests = new ArrayList<>(potentialTargets.size());
        for (LivingEntity target : potentialTargets) {
            if (target == shooter || target == hitEntity) {
                continue;
            }
            Vec3 delta = new Vec3(target.getX() - impactPosition.x,
                    target.getY() + target.getBbHeight() / 2.0D - impactPosition.y,
                    target.getZ() - impactPosition.z);
            if (delta.length() > LIGHTNING_SPLIT_RANGE || delta.lengthSqr() <= 1.0E-7D) {
                continue;
            }
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(subBeam, impactPosition,
                    delta.normalize(), 1.0F, 0.0F, roll);
            requests.add(new SpawnRequest(SpawnType.LIGHTNING_SPLIT_SUB_BEAM, subBeam, plan, shooter, target,
                    impactPosition, damage));
        }
        return Collections.unmodifiableList(requests);
    }

    public static List<SpawnRequest> collectImpactSpawnRequests(BulletConfig config, Level level,
            @Nullable Entity shooter, Vec3 impactPosition, @Nullable RandomSource random) {
        return collectImpactSpawnRequests(config, level, shooter, impactPosition, random, null, 0.0F);
    }

    public static List<SpawnRequest> collectImpactSpawnRequests(BulletConfig config, Level level,
            @Nullable Entity shooter, Vec3 impactPosition, @Nullable RandomSource random,
            @Nullable Direction blockSide, float impactDamage) {
        if (config == null || level == null || impactPosition == null || level.isClientSide()
                || (!config.hasBehavior(BulletBehaviorTag.MASKMAN_TRACER_METEOR)
                && !config.hasBehavior(BulletBehaviorTag.SHREDDER_BEAM_SPLIT))) {
            return Collections.emptyList();
        }

        if (config.hasBehavior(BulletBehaviorTag.SHREDDER_BEAM_SPLIT)) {
            return collectShredderSubmunitions(config, level, shooter, impactPosition, random, blockSide,
                    impactDamage);
        }

        RandomSource roll = random == null ? level.random : random;
        Vec3 position = impactPosition.add(0.0D,
                MASKMAN_METEOR_MIN_Y_OFFSET + roll.nextInt(MASKMAN_METEOR_EXTRA_Y_RANGE), 0.0D);
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(LegacyBulletConfigs.MASKMAN_METEOR,
                position, MASKMAN_METEOR_MOTION, 1.0F, 0.0F, roll);
        return Collections.singletonList(new SpawnRequest(SpawnType.MASKMAN_TRACER_METEOR,
                LegacyBulletConfigs.MASKMAN_METEOR, plan, shooter, null, impactPosition));
    }

    private static List<SpawnRequest> collectShredderSubmunitions(BulletConfig config, Level level,
            @Nullable Entity shooter, Vec3 impactPosition, @Nullable RandomSource random,
            @Nullable Direction blockSide, float impactDamage) {
        BulletConfig submunition = shredderSubmunition(config);
        if (submunition == null) {
            return Collections.emptyList();
        }

        RandomSource roll = random == null ? level.random : random;
        int projectiles = BulletLaunchUtil.rollProjectileCount(submunition, roll);
        if (projectiles <= 0) {
            return Collections.emptyList();
        }

        Vec3 origin = blockSide == null ? impactPosition : impactPosition.add(blockSide.getStepX() * SHREDDER_BLOCK_SIDE_OFFSET,
                blockSide.getStepY() * SHREDDER_BLOCK_SIDE_OFFSET,
                blockSide.getStepZ() * SHREDDER_BLOCK_SIDE_OFFSET);
        float damage = impactDamage > 0.0F ? impactDamage * submunition.damageMax() : 0.0F;
        float deviation = submunition.spread() + SHREDDER_SUBMUNITION_GUN_SPREAD;
        List<SpawnRequest> requests = new ArrayList<>(projectiles);
        for (int i = 0; i < projectiles; i++) {
            Vec3 heading = blockSide == null ? randomUnitVector(roll)
                    : new Vec3(blockSide.getStepX(), blockSide.getStepY(), blockSide.getStepZ());
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(submunition, origin, heading,
                    BulletKinematicsUtil.DEFAULT_THROW_FORCE, deviation, roll);
            requests.add(new SpawnRequest(SpawnType.SHREDDER_SUBMUNITION, submunition, plan, shooter, null,
                    impactPosition, damage));
        }
        return Collections.unmodifiableList(requests);
    }

    @Nullable
    private static BulletConfig shredderSubmunition(BulletConfig config) {
        return switch (config.legacyName()) {
            case "g12_shredder" -> LegacySednaRuntimeBulletConfigs.G12_SUB;
            case "g12_shredder_slug" -> LegacySednaRuntimeBulletConfigs.G12_SUB_SLUG;
            case "g12_shredder_flechette" -> LegacySednaRuntimeBulletConfigs.G12_SUB_FLECHETTE;
            case "g12_shredder_magnum" -> LegacySednaRuntimeBulletConfigs.G12_SUB_MAGNUM;
            case "g12_shredder_explosive" -> LegacySednaRuntimeBulletConfigs.G12_SUB_EXPLOSIVE;
            case "g12_shredder_phosphorus" -> LegacySednaRuntimeBulletConfigs.G12_SUB_PHOSPHORUS;
            default -> null;
        };
    }

    private static BulletLaunchUtil.LaunchPlan legacyYawPitchLaunchPlan(BulletConfig config, Vec3 origin,
            float yaw, float pitch, RandomSource random) {
        Vec3 heading = new Vec3(-Mth.sin(yaw) * Mth.cos(pitch),
                Mth.sin(pitch),
                Mth.cos(yaw) * Mth.cos(pitch));
        return BulletLaunchUtil.directedLaunchPlan(config, origin, heading, 1.0F, 0.0F, random);
    }

    private static Vec3 randomUnitVector(RandomSource random) {
        Vec3 vector;
        do {
            vector = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
        } while (vector.lengthSqr() <= 1.0E-7D);
        return vector.normalize();
    }

    public enum SpawnType {
        MASKMAN_MINIGUN_BULLET,
        MASKMAN_LASER_ORB,
        MASKMAN_LASER_MISSILE,
        MASKMAN_LASER_TRACER,
        MASKMAN_ORB_BOLT,
        MASKMAN_TRACER_METEOR,
        SHREDDER_SUBMUNITION,
        LIGHTNING_SPLIT_SUB_BEAM,
        GRENADE_FRAGMENTATION,
        GRENADE_CLUSTER_PELLET,
        GRENADE_CLUSTER_HEAVY_PELLET,
        GRENADE_LASER_BEAM
    }

    public record SpawnRequest(SpawnType type, BulletConfig config, BulletLaunchUtil.LaunchPlan launchPlan,
            @Nullable Entity shooter, @Nullable Entity target, Vec3 triggerPosition, float overrideDamage) {
        public SpawnRequest(SpawnType type, BulletConfig config, BulletLaunchUtil.LaunchPlan launchPlan,
                @Nullable Entity shooter, @Nullable Entity target, Vec3 triggerPosition) {
            this(type, config, launchPlan, shooter, target, triggerPosition, 0.0F);
        }
    }

    private BulletSpecialSpawnUtil() {
    }
}
