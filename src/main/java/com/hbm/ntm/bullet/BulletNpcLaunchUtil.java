package com.hbm.ntm.bullet;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulletNpcLaunchUtil {
    public static final double MASKMAN_MINIGUN_MIN_RANGE = 5.0D;
    public static final double MASKMAN_MINIGUN_MAX_RANGE = 10.0D;
    public static final double MASKMAN_LASERGUN_MIN_RANGE = 10.0D;
    public static final int MASKMAN_SPLASH_TRACER_COUNT = 5;

    public static final String MASKMAN_MINIGUN_SOUND = "weapon.calShoot";
    public static final String MASKMAN_ORB_SOUND = "weapon.teslaShoot";
    public static final String MASKMAN_MISSILE_SOUND = "weapon.hkShoot";

    public static boolean shouldMaskmanMinigunExecute(LivingEntity owner, LivingEntity target) {
        double distance = distance(owner, target);
        return distance > MASKMAN_MINIGUN_MIN_RANGE && distance < MASKMAN_MINIGUN_MAX_RANGE;
    }

    public static boolean shouldMaskmanLasergunExecute(LivingEntity owner, LivingEntity target) {
        return distance(owner, target) > MASKMAN_LASERGUN_MIN_RANGE;
    }

    public static NpcAttackRequest maskmanMinigunShot(LivingEntity owner, LivingEntity target,
            RandomSource random) {
        if (owner == null || target == null) {
            return NpcAttackRequest.NONE;
        }
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.aimedLaunchPlan(LegacyBulletConfigs.MASKMAN_BULLET,
                owner, target, 1.0F, 0.0F, random);
        return single(BulletSpecialSpawnUtil.SpawnType.MASKMAN_MINIGUN_BULLET, LegacyBulletConfigs.MASKMAN_BULLET,
                plan, owner, target, MASKMAN_MINIGUN_SOUND);
    }

    public static NpcAttackRequest maskmanLasergunShot(LivingEntity owner, LivingEntity target,
            MaskmanLaserAttack attack, RandomSource random) {
        if (owner == null || target == null || attack == null) {
            return NpcAttackRequest.NONE;
        }
        return switch (attack) {
            case ORB -> maskmanOrbShot(owner, target, random);
            case MISSILE -> maskmanMissileShot(owner, target, random);
            case SPLASH -> maskmanSplashShot(owner, target, random);
        };
    }

    public static MaskmanLaserAttack initialMaskmanLaserAttack(RandomSource random) {
        MaskmanLaserAttack[] values = MaskmanLaserAttack.values();
        RandomSource roll = random == null ? RandomSource.create() : random;
        return values[roll.nextInt(values.length)];
    }

    public static MaskmanLaserAttack nextMaskmanLaserAttack(MaskmanLaserAttack current, RandomSource random) {
        if (current == null) {
            return initialMaskmanLaserAttack(random);
        }
        MaskmanLaserAttack[] values = MaskmanLaserAttack.values();
        RandomSource roll = random == null ? RandomSource.create() : random;
        int next = current.ordinal() + roll.nextInt(values.length - 1);
        return values[next % values.length];
    }

    private static NpcAttackRequest maskmanOrbShot(LivingEntity owner, LivingEntity target, RandomSource random) {
        BulletLaunchUtil.LaunchPlan base = BulletLaunchUtil.aimedLaunchPlan(LegacyBulletConfigs.MASKMAN_ORB,
                owner, target, 2.0F, 0.0F, random);
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.offsetMotion(base, new Vec3(0.0D, 0.5D, 0.0D));
        return single(BulletSpecialSpawnUtil.SpawnType.MASKMAN_LASER_ORB, LegacyBulletConfigs.MASKMAN_ORB,
                plan, owner, target, MASKMAN_ORB_SOUND);
    }

    private static NpcAttackRequest maskmanMissileShot(LivingEntity owner, LivingEntity target,
            RandomSource random) {
        BulletLaunchUtil.LaunchPlan base = BulletLaunchUtil.aimedLaunchPlan(LegacyBulletConfigs.MASKMAN_ROCKET,
                owner, target, 1.0F, 0.0F, random);
        RandomSource roll = random == null ? owner.getRandom() : random;
        Vec3 motion = new Vec3((target.getX() - owner.getX()) * 0.05D,
                0.5D + roll.nextDouble() * 0.5D,
                (target.getZ() - owner.getZ()) * 0.05D);
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.withMotion(base, motion);
        return single(BulletSpecialSpawnUtil.SpawnType.MASKMAN_LASER_MISSILE, LegacyBulletConfigs.MASKMAN_ROCKET,
                plan, owner, target, MASKMAN_MISSILE_SOUND);
    }

    private static NpcAttackRequest maskmanSplashShot(LivingEntity owner, LivingEntity target,
            RandomSource random) {
        List<BulletSpecialSpawnUtil.SpawnRequest> requests = new ArrayList<>(MASKMAN_SPLASH_TRACER_COUNT);
        for (int i = 0; i < MASKMAN_SPLASH_TRACER_COUNT; i++) {
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.aimedLaunchPlan(LegacyBulletConfigs.MASKMAN_TRACER,
                    owner, target, 1.0F, 0.05F, random);
            requests.add(new BulletSpecialSpawnUtil.SpawnRequest(
                    BulletSpecialSpawnUtil.SpawnType.MASKMAN_LASER_TRACER,
                    LegacyBulletConfigs.MASKMAN_TRACER, plan, owner, target, owner.position()));
        }
        return new NpcAttackRequest(Collections.unmodifiableList(requests), "");
    }

    private static NpcAttackRequest single(BulletSpecialSpawnUtil.SpawnType type, BulletConfig config,
            BulletLaunchUtil.LaunchPlan plan, LivingEntity owner, LivingEntity target, String soundName) {
        BulletSpecialSpawnUtil.SpawnRequest request =
                new BulletSpecialSpawnUtil.SpawnRequest(type, config, plan, owner, target, owner.position());
        return new NpcAttackRequest(Collections.singletonList(request), soundName);
    }

    private static double distance(LivingEntity owner, LivingEntity target) {
        if (owner == null || target == null) {
            return 0.0D;
        }
        return new Vec3(target.getX() - owner.getX(), target.getY() - owner.getY(),
                target.getZ() - owner.getZ()).length();
    }

    public enum MaskmanLaserAttack {
        ORB(60, 5),
        MISSILE(10, 10),
        SPLASH(40, 3);

        private final int delay;
        private final int amount;

        MaskmanLaserAttack(int delay, int amount) {
            this.delay = delay;
            this.amount = amount;
        }

        public int delay() {
            return delay;
        }

        public int amount() {
            return amount;
        }
    }

    public record NpcAttackRequest(List<BulletSpecialSpawnUtil.SpawnRequest> spawnRequests, String legacySoundName) {
        public static final NpcAttackRequest NONE = new NpcAttackRequest(Collections.emptyList(), "");
    }

    private BulletNpcLaunchUtil() {
    }
}
