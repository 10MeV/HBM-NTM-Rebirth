package com.hbm.ntm.bullet;

import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.entity.logic.C130Entity;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.RayTraceUtil;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BulletUpdateBehaviorUtil {
    private static final double FOLLY_SUPERMATTER_RANGE = 250.0D;
    private static final double FOLLY_SUPERMATTER_VISUAL_SPACING = 10.0D;
    private static final int FOLLY_SUPERMATTER_EFFECT_TICK = 2;
    private static final int FOLLY_SUPERMATTER_VISUAL_TICKS = 50;
    private static final float FOLLY_SUPERMATTER_SHOOTER_RADIATION = 150.0F;
    private static final float FOLLY_SUPERMATTER_DT_NEGATION = 100.0F;
    private static final float FOLLY_SUPERMATTER_DR_PIERCING = 0.99F;
    private static final double ROCKET_STEERING_PLAYER_RANGE_SQ = 100.0D * 100.0D;
    private static final double ROCKET_STEERING_MIN_TARGET_RANGE_SQ = 3.0D * 3.0D;

    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable Entity currentHomingTarget) {
        return applyKnownPreMoveUpdate(config, projectile, shooter, motion, currentHomingTarget, null, 0.0F);
    }

    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable Entity currentHomingTarget,
            @Nullable Vec3 previousPosition) {
        return applyKnownPreMoveUpdate(config, projectile, shooter, motion, currentHomingTarget, previousPosition,
                0.0F);
    }

    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable Entity currentHomingTarget,
            @Nullable Vec3 previousPosition, float currentAcceleration) {
        return applyKnownPreMoveUpdate(config, projectile, shooter, motion, currentHomingTarget, previousPosition,
                currentAcceleration, 0.0F);
    }

    public static KnownUpdateResult applyKnownPreMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Vec3 motion, @Nullable Entity currentHomingTarget,
            @Nullable Vec3 previousPosition, float currentAcceleration, float overrideDamage) {
        if (config == null || projectile == null || motion == null) {
            return new KnownUpdateResult(motion, currentHomingTarget, false, false, 0, currentAcceleration, false);
        }

        float acceleration = currentAcceleration;
        Vec3 updatedMotion = motion;
        int brokenInPath = applyCoilBreakInPath(config, projectile.level(),
                previousPosition == null ? projectile.position().subtract(
                        BulletKinematicsUtil.movementDelta(config, motion, currentAcceleration))
                        : previousPosition,
                projectile.position());
        if (projectile.level().isClientSide()) {
            return new KnownUpdateResult(updatedMotion, currentHomingTarget, false, false, brokenInPath,
                    acceleration, acceleration != currentAcceleration);
        }
        if (applyFireExtinguisherWaterUpdate(config, projectile)) {
            return new KnownUpdateResult(updatedMotion, currentHomingTarget, false, false, brokenInPath,
                    acceleration, acceleration != currentAcceleration, true);
        }
        if (config.hasBehavior(BulletBehaviorTag.CHLOROPHYTE_HOMING)) {
            LivingEntity currentLivingTarget = currentHomingTarget instanceof LivingEntity living ? living : null;
            HomingResult homing = updateHoming(projectile, shooter, updatedMotion, currentLivingTarget,
                    BulletHomingUtil.CHLOROPHYTE_RANGE, BulletHomingUtil.CHLOROPHYTE_ANGLE);
            return new KnownUpdateResult(homing.motion(), homing.target(), homing.acquiredTarget(), false,
                    brokenInPath, acceleration, acceleration != currentAcceleration);
        }

        return new KnownUpdateResult(updatedMotion, currentHomingTarget, false, false, brokenInPath,
                acceleration, acceleration != currentAcceleration);
    }

    public static void applyKnownBeamUpdate(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            Vec3 motion, float overrideDamage, double beamLength) {
        if (config == null || projectile == null || motion == null) {
            return;
        }
        if (config.hasBehavior(BulletBehaviorTag.FOLLY_SUPERMATTER_BEAM)) {
            applyFollySupermatterBeam(config, projectile, shooter, motion, overrideDamage, beamLength);
        }
    }

    public static KnownPostMoveResult applyKnownPostMoveUpdate(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, @Nullable Entity currentHomingTarget, Vec3 position, Vec3 motion,
            float currentAcceleration) {
        if (config == null || projectile == null || position == null || motion == null) {
            return new KnownPostMoveResult(motion, currentHomingTarget, currentAcceleration, false);
        }
        float acceleration = applyRocketAcceleration(config, shooter, currentAcceleration);
        applyAirdropFlare(config, projectile, shooter, position);
        Vec3 updatedMotion = applyRocketSteering(config, shooter, position, motion);
        if (currentHomingTarget != null && currentHomingTarget.isAlive() && !hasAutonomousHoming(config)) {
            updatedMotion = BulletHomingUtil.steerLegacyLockOn(currentHomingTarget, position,
                    updatedMotion, projectile.tickCount);
        }
        return new KnownPostMoveResult(updatedMotion, currentHomingTarget, acceleration,
                acceleration != currentAcceleration);
    }

    private static void applyAirdropFlare(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            Vec3 postMovePosition) {
        if (projectile.level().isClientSide() || projectile.tickCount != 40) {
            return;
        }
        C130Entity.Payload payload = config.hasBehavior(BulletBehaviorTag.AIRDROP_SUPPLIES)
                ? C130Entity.Payload.SUPPLIES
                : config.hasBehavior(BulletBehaviorTag.AIRDROP_WEAPONS) ? C130Entity.Payload.WEAPONS : null;
        if (payload == null) {
            return;
        }
        if (shooter != null) {
            LegacySoundPlayer.playLegacyTechBleep(shooter, 1.0F, 1.0F);
        }
        int x = Mth.floor(postMovePosition.x);
        int z = Mth.floor(postMovePosition.z);
        int y = projectile.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        WorldUtil.loadAndSpawnEntityInWorld(C130Entity.create(projectile.level(), x, y, z, payload));
    }

    private static void applyFollySupermatterBeam(BulletConfig config, Entity projectile, @Nullable Entity shooter,
            Vec3 motion, float overrideDamage, double beamLength) {
        if (projectile.level().isClientSide()) {
            return;
        }

        Vec3 direction = motion.lengthSqr() > 1.0E-7D ? motion.normalize() : legacyBulletRotationDirection(projectile);
        if (direction.lengthSqr() <= 1.0E-7D) {
            return;
        }
        double effectiveBeamLength = beamLength > 1.0E-7D ? beamLength : FOLLY_SUPERMATTER_RANGE;
        double originX = projectile.getX();
        double originY = projectile.getY();
        double originZ = projectile.getZ();
        spawnFollySupermatterVisual(projectile.level(), originX, originY, originZ, direction,
                projectile.getXRot(), projectile.getYRot(), projectile.tickCount, effectiveBeamLength);

        if (projectile.tickCount != FOLLY_SUPERMATTER_EFFECT_TICK) {
            return;
        }
        if (shooter instanceof LivingEntity livingShooter) {
            RadiationUtil.contaminate(livingShooter, HazardType.RADIATION,
                    RadiationUtil.ContaminationType.CREATIVE, FOLLY_SUPERMATTER_SHOOTER_RADIATION);
        }

        AABB beamArea = projectile.getBoundingBox()
                .expandTowards(direction.x * effectiveBeamLength, direction.y * effectiveBeamLength,
                        direction.z * effectiveBeamLength)
                .inflate(1.0D);
        java.util.List<Entity> entities = projectile.level().getEntities(projectile, beamArea,
                entity -> entity.isAlive() && entity != shooter);
        float damage = overrideDamage > 0.0F ? overrideDamage : config.damageMax();
        int minY = projectile.level().getMinBuildHeight();
        int maxY = projectile.level().getMaxBuildHeight();
        BlockPos.MutableBlockPos clearPos = new BlockPos.MutableBlockPos();
        for (int distance = 1; distance < effectiveBeamLength; distance += 2) {
            int x = (int) Math.floor(originX + direction.x * distance);
            int y = (int) Math.floor(originY + direction.y * distance);
            int z = (int) Math.floor(originZ + direction.z * distance);

            for (int ix = x - 1; ix <= x + 1; ix++) {
                for (int iy = y - 1; iy <= y + 1; iy++) {
                    if (iy < minY || iy >= maxY) {
                        continue;
                    }
                    for (int iz = z - 1; iz <= z + 1; iz++) {
                        clearPos.set(ix, iy, iz);
                        if (projectile.level().hasChunkAt(clearPos)) {
                            projectile.level().setBlock(clearPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                        AABB cell = new AABB(ix - 1.0D, iy - 1.0D, iz - 1.0D,
                                ix + 2.0D, iy + 2.0D, iz + 2.0D);
                        for (Entity entity : entities) {
                            if (entity.getBoundingBox().intersects(cell)) {
                                applyFollySupermatterDamage(config, projectile, shooter, entity, damage);
                            }
                        }
                    }
                }
            }
        }
    }

    private static Vec3 legacyBulletRotationDirection(Entity projectile) {
        float yaw = projectile.getYRot() * ((float) Math.PI / 180.0F);
        float pitch = projectile.getXRot() * ((float) Math.PI / 180.0F);
        return new Vec3(Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
    }

    private static void spawnFollySupermatterVisual(Level level, double originX, double originY, double originZ,
            Vec3 direction,
            float pitch, float yaw, int ticksExisted, double beamLength) {
        if (ticksExisted >= FOLLY_SUPERMATTER_VISUAL_TICKS) {
            return;
        }
        double distance = ticksExisted * FOLLY_SUPERMATTER_VISUAL_SPACING;
        double x = originX + direction.x * distance;
        double y = originY + direction.y * distance;
        double z = originZ + direction.z * distance;
        double scaledBeamLength = Math.max(beamLength, 1.0E-7D);
        float scale = 2.0F + ticksExisted / (float) (scaledBeamLength / FOLLY_SUPERMATTER_VISUAL_SPACING)
                * 3.0F;
        ParticleUtil.spawnPlasmaBlast(level, x, y, z,
                0.75F, 0.75F, 0.75F, pitch + 90.0F, -yaw, scale, 250.0D);
    }

    private static void applyFollySupermatterDamage(BulletConfig config, Entity projectile,
            @Nullable Entity shooter, Entity target, float damage) {
        if (target instanceof LivingEntity) {
            EntityDamageUtil.attackEntityFromNt(target, config.damageSource(projectile.level(), projectile, shooter),
                    damage, true, false, 0.0D, FOLLY_SUPERMATTER_DT_NEGATION, FOLLY_SUPERMATTER_DR_PIERCING);
        } else {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(target,
                    config.damageSource(projectile.level(), projectile, shooter), damage);
        }
    }

    private static boolean hasAutonomousHoming(BulletConfig config) {
        return config.hasBehavior(BulletBehaviorTag.CHLOROPHYTE_HOMING);
    }

    private static boolean applyFireExtinguisherWaterUpdate(BulletConfig config, Entity projectile) {
        if (!config.hasBehavior(BulletBehaviorTag.FIRE_EXTINGUISH_WATER)
                || projectile == null || projectile.level().isClientSide()) {
            return false;
        }
        BlockPos pos = BlockPos.containing(projectile.position());
        if (!projectile.level().getBlockState(pos).is(ModBlocks.VOLCANIC_LAVA_BLOCK.get())) {
            return false;
        }
        return projectile.level().setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
    }

    public static HomingResult updateHoming(Entity projectile, @Nullable Entity shooter, Vec3 motion,
            @Nullable LivingEntity currentTarget, double range, double angle) {
        if (projectile == null || motion == null) {
            return new HomingResult(motion, currentTarget, false);
        }

        LivingEntity target = currentTarget != null && currentTarget.isAlive() ? currentTarget : null;
        boolean acquired = false;
        if (target == null) {
            Optional<LivingEntity> found = BulletHomingUtil.findTarget(projectile, shooter, motion, range, angle);
            target = found.orElse(null);
            acquired = target != null;
        }

        Vec3 steered = target == null ? motion : BulletHomingUtil.steerTowards(target, projectile.position(), motion);
        return new HomingResult(steered, target, acquired);
    }

    private static int applyCoilBreakInPath(BulletConfig config, Level level, Vec3 previousPosition,
            Vec3 currentPosition) {
        float threshold = coilBreakThreshold(config);
        if (threshold <= 0.0F || level == null || previousPosition == null || currentPosition == null) {
            return 0;
        }
        double deltaX = currentPosition.x - previousPosition.x;
        double deltaY = currentPosition.y - previousPosition.y;
        double deltaZ = currentPosition.z - previousPosition.z;
        double distanceSqr = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        double trueDistance = Math.sqrt(distanceSqr);
        double motion = Math.max(trueDistance, 0.1D);
        double invDistance = distanceSqr < 1.0E-7D ? 0.0D : 1.0D / trueDistance;
        double directionX = deltaX * invDistance;
        double directionY = deltaY * invDistance;
        double directionZ = deltaZ * invDistance;
        BlockPos.MutableBlockPos mutablePos = level.isClientSide() ? null : new BlockPos.MutableBlockPos();
        int changed = 0;
        for (double distance = 0.0D; distance < motion; distance += 0.5D) {
            double pointX = currentPosition.x - directionX * distance;
            double pointY = currentPosition.y - directionY * distance;
            double pointZ = currentPosition.z - directionZ * distance;
            if (level.isClientSide()) {
                ParticleUtil.spawnVanillaExt(level, pointX, pointY, pointZ, ParticleUtil.VANILLA_FIREWORKS,
                        0.0D, 0.0D, 0.0D);
                changed++;
                continue;
            }
            mutablePos.set((int) Math.floor(pointX), (int) Math.floor(pointY), (int) Math.floor(pointZ));
            BlockState state = level.getBlockState(mutablePos);
            float hardness = state.getDestroySpeed(level, mutablePos);
            if (!state.isAir() && hardness >= 0.0F && hardness < threshold
                    && level.destroyBlock(mutablePos.immutable(), false)) {
                changed++;
            }
        }
        return changed;
    }

    private static float coilBreakThreshold(BulletConfig config) {
        if (config.hasBehavior(BulletBehaviorTag.COIL_BREAK_WEAK_BLOCKS)) {
            return 1.25F;
        }
        if (config.hasBehavior(BulletBehaviorTag.COIL_BREAK_STRONGER_BLOCKS)) {
            return 2.5F;
        }
        return 0.0F;
    }

    private static float applyRocketAcceleration(BulletConfig config, @Nullable Entity shooter,
            float currentAcceleration) {
        if (config.hasBehavior(BulletBehaviorTag.ROCKET_STEER)) {
            float limit = isQdSteeringRocket(config) && !(shooter instanceof Player) ? 7.0F : 4.0F;
            return currentAcceleration < limit ? currentAcceleration + 0.4F : currentAcceleration;
        }
        if (config.hasBehavior(BulletBehaviorTag.ROCKET_ACCELERATE)) {
            return currentAcceleration < 7.0F ? currentAcceleration + 0.4F : currentAcceleration;
        }
        return currentAcceleration;
    }

    private static Vec3 applyRocketSteering(BulletConfig config, @Nullable Entity shooter, Vec3 position,
            Vec3 motion) {
        if (!config.hasBehavior(BulletBehaviorTag.ROCKET_STEER)
                || !(shooter instanceof Player player)
                || !canSteerRocket(config, player)
                || distanceToSqr(position, player) > ROCKET_STEERING_PLAYER_RANGE_SQ) {
            return motion;
        }

        HitResult hit = RayTraceUtil.getMouseOver(player, 200.0D, 0.0D, 1.0F);
        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            return motion;
        }
        Vec3 target = hit.getLocation().subtract(position);
        if (target.lengthSqr() < ROCKET_STEERING_MIN_TARGET_RANGE_SQ || motion.lengthSqr() <= 1.0E-7D) {
            return motion;
        }
        return target.normalize().scale(motion.length());
    }

    private static double distanceToSqr(Vec3 position, Entity entity) {
        double dx = position.x - entity.getX();
        double dy = position.y - entity.getY();
        double dz = position.z - entity.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean isAlwaysSteeringRocket(BulletConfig config) {
        return config.legacyName().startsWith("rocket_ncrpa_steer");
    }

    private static boolean isQdSteeringRocket(BulletConfig config) {
        return config.legacyName().startsWith("rocket_qd_");
    }

    private static boolean canSteerRocket(BulletConfig config, Player player) {
        if (isAlwaysSteeringRocket(config)) {
            return true;
        }
        return isQdSteeringRocket(config) && isAimedSednaGun(player.getMainHandItem());
    }

    private static boolean isAimedSednaGun(net.minecraft.world.item.ItemStack stack) {
        return stack.getItem() instanceof SednaGunItem gun && gun.legacyIsAiming(stack);
    }

    public record KnownUpdateResult(Vec3 motion, @Nullable Entity homingTarget, boolean acquiredHomingTarget,
            boolean triggeredUfoBlast, int coilBlocksBroken, float acceleration, boolean accelerated,
            boolean discardProjectile) {
        public static final KnownUpdateResult NONE = new KnownUpdateResult(Vec3.ZERO, null, false, false, 0,
                0.0F, false);

        public KnownUpdateResult(Vec3 motion, @Nullable Entity homingTarget, boolean acquiredHomingTarget,
                boolean triggeredUfoBlast, int coilBlocksBroken, float acceleration, boolean accelerated) {
            this(motion, homingTarget, acquiredHomingTarget, triggeredUfoBlast, coilBlocksBroken, acceleration,
                    accelerated, triggeredUfoBlast);
        }
    }

    public record HomingResult(Vec3 motion, @Nullable LivingEntity target, boolean acquiredTarget) {
    }

    public record KnownPostMoveResult(Vec3 motion, @Nullable Entity homingTarget, float acceleration,
            boolean accelerated) {
    }

    private BulletUpdateBehaviorUtil() {
    }
}
