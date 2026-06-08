package com.hbm.ntm.bullet;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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

    public static List<SpawnRequest> collectImpactSpawnRequests(BulletConfig config, Level level,
            @Nullable Entity shooter, Vec3 impactPosition, @Nullable RandomSource random) {
        if (config == null || level == null || impactPosition == null || level.isClientSide()
                || !config.hasBehavior(BulletBehaviorTag.MASKMAN_TRACER_METEOR)) {
            return Collections.emptyList();
        }

        RandomSource roll = random == null ? level.random : random;
        Vec3 position = impactPosition.add(0.0D,
                MASKMAN_METEOR_MIN_Y_OFFSET + roll.nextInt(MASKMAN_METEOR_EXTRA_Y_RANGE), 0.0D);
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedLaunchPlan(LegacyBulletConfigs.MASKMAN_METEOR,
                position, MASKMAN_METEOR_MOTION, 1.0F, 0.0F, roll);
        return Collections.singletonList(new SpawnRequest(SpawnType.MASKMAN_TRACER_METEOR,
                LegacyBulletConfigs.MASKMAN_METEOR, plan, shooter, null, impactPosition));
    }

    public enum SpawnType {
        MASKMAN_MINIGUN_BULLET,
        MASKMAN_LASER_ORB,
        MASKMAN_LASER_MISSILE,
        MASKMAN_LASER_TRACER,
        MASKMAN_ORB_BOLT,
        MASKMAN_TRACER_METEOR
    }

    public record SpawnRequest(SpawnType type, BulletConfig config, BulletLaunchUtil.LaunchPlan launchPlan,
            @Nullable Entity shooter, @Nullable Entity target, Vec3 triggerPosition) {
    }

    private BulletSpecialSpawnUtil() {
    }
}
