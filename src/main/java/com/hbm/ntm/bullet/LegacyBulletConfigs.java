package com.hbm.ntm.bullet;

import com.hbm.ntm.radiation.ModDamageSources;

public final class LegacyBulletConfigs {
    public static final float DEFAULT_SPREAD = 0.005F;

    public static final BulletConfig TURBINE = turbine();
    public static final BulletConfig MASKMAN_BULLET = maskmanBullet();
    public static final BulletConfig MASKMAN_ORB = maskmanOrb();
    public static final BulletConfig MASKMAN_BOLT = maskmanBolt();
    public static final BulletConfig MASKMAN_ROCKET = maskmanRocket();
    public static final BulletConfig MASKMAN_TRACER = maskmanTracer();
    public static final BulletConfig MASKMAN_METEOR = maskmanMeteor();
    public static final BulletConfig WORM_BOLT = wormBolt();
    public static final BulletConfig WORM_LASER = wormHeadBolt();
    public static final BulletConfig UFO_ROCKET = rocketUfo();

    public static BulletConfig standardBullet(String legacyName) {
        return BulletConfig.builder(legacyName)
                .ballistics(5.0F, DEFAULT_SPREAD, 10, 1, 1)
                .physics(0.0D, 100)
                .ricochet(true, 5.0D, 95, 2, 0.8D)
                .penetration(true)
                .breaksGlass(true)
                .destroysBlocks(false)
                .appearance(BulletStyle.NORMAL, 0, BulletPlink.BULLET, "")
                .leadChance(5)
                .build();
    }

    public static BulletConfig standardRocket(String legacyName) {
        return BulletConfig.builder(legacyName)
                .ballistics(2.0F, DEFAULT_SPREAD, 10, 1, 1)
                .physics(0.005D, 300)
                .ricochet(true, 10.0D, 100, 2, 0.8D)
                .penetration(false)
                .breaksGlass(false)
                .explosive(5.0F)
                .appearance(BulletStyle.ROCKET, 0, BulletPlink.GRENADE, "smoke")
                .build();
    }

    public static BulletConfig standardGrenade(String legacyName) {
        return BulletConfig.builder(legacyName)
                .ballistics(2.0F, DEFAULT_SPREAD, 10, 1, 1)
                .physics(0.035D, 300)
                .ricochet(false, 0.0D, 0, 0, 1.0D)
                .penetration(false)
                .breaksGlass(false)
                .explosive(2.5F)
                .appearance(BulletStyle.GRENADE, 0, BulletPlink.GRENADE, "smoke")
                .build();
    }

    public static BulletConfig chlorophyte(BulletConfig base, BulletAmmo ammo) {
        return base.toBuilder()
                .ammo(ammo)
                .ballistics(base.velocity() * 0.3F, base.spread(), (int) (base.wear() * 0.5D),
                        base.bulletsMin(), base.bulletsMax())
                .damage(base.damageMin() * 2.0F, base.damageMax() * 2.0F)
                .ricochet(false, base.ricochetAngle(), base.lowerBoundRicochetChance(),
                        base.higherBoundRicochetChance(), base.bounceModifier())
                .penetration(true)
                .appearance(base.style(), base.trail(), base.plink(), "greendust")
                .behavior(BulletBehaviorTag.CHLOROPHYTE_HOMING)
                .behavior(BulletBehaviorTag.PENETRATION_HOMING_RESET)
                .build();
    }

    private static BulletConfig turbine() {
        return BulletConfig.builder("turbine")
                .ammo(BulletAmmo.NOTHING)
                .damage(100.0F, 150.0F)
                .ballistics(1.0F, 0.0F, 0, 0, 0)
                .physics(0.0D, 200)
                .ricochet(false, 0.0D, 0, 0, 0.0D)
                .destroysBlocks(true)
                .appearance(BulletStyle.BLADE, 0, BulletPlink.NONE, "")
                .build();
    }

    private static BulletConfig maskmanOrb() {
        return BulletConfig.builder("maskman_orb")
                .ammo(BulletAmmo.legacyItem("coin_maskman"))
                .ballistics(0.25F, 0.0F, 10, 1, 1)
                .damage(100.0F, 100.0F)
                .physics(0.0D, 60)
                .ricochet(false, 0.0D, 0, 0, 1.0D)
                .penetration(false)
                .breaksGlass(false)
                .explosive(1.5F)
                .appearance(BulletStyle.ORB, 1, BulletPlink.NONE, "")
                .behavior(BulletBehaviorTag.MASKMAN_ORB_BOLT_VOLLEY)
                .build();
    }

    private static BulletConfig maskmanBolt() {
        return standardBullet("maskman_bolt").toBuilder()
                .ammo(BulletAmmo.legacyItem("coin_maskman"))
                .ballistics(5.0F, 0.0F, 10, 1, 1)
                .damage(15.0F, 20.0F)
                .leadChance(0)
                .explosive(0.5F)
                .setToBolt(BulletTrail.LACUNAE)
                .appearance(BulletStyle.BOLT, BulletTrail.LACUNAE.legacyId(), BulletPlink.BULLET, "reddust")
                .damageType(ModDamageSources.LASER)
                .build();
    }

    private static BulletConfig maskmanBullet() {
        return standardBullet("maskman_bullet").toBuilder()
                .ammo(BulletAmmo.legacyItem("coin_maskman"))
                .ballistics(5.0F, 0.0F, 10, 1, 1)
                .damage(5.0F, 10.0F)
                .leadChance(15)
                .appearance(BulletStyle.FLECHETTE, 0, BulletPlink.BULLET, "bluedust")
                .build();
    }

    private static BulletConfig maskmanTracer() {
        return standardBullet("maskman_tracer").toBuilder()
                .ammo(BulletAmmo.legacyItem("coin_maskman"))
                .ballistics(5.0F, 0.0F, 10, 1, 1)
                .damage(15.0F, 20.0F)
                .leadChance(0)
                .setToBolt(BulletTrail.NIGHTMARE)
                .appearance(BulletStyle.BOLT, BulletTrail.NIGHTMARE.legacyId(), BulletPlink.BULLET, "reddust")
                .damageType(ModDamageSources.LASER)
                .behavior(BulletBehaviorTag.MASKMAN_TRACER_METEOR)
                .build();
    }

    private static BulletConfig maskmanRocket() {
        return standardGrenade("maskman_rocket").toBuilder()
                .ammo(BulletAmmo.legacyItem("coin_maskman"))
                .ballistics(1.0F, DEFAULT_SPREAD, 10, 1, 1)
                .physics(0.1D, 300)
                .damage(15.0F, 20.0F)
                .blockDamage(false)
                .explosive(5.0F)
                .appearance(BulletStyle.ROCKET, 0, BulletPlink.GRENADE, "smoke")
                .build();
    }

    private static BulletConfig maskmanMeteor() {
        return standardGrenade("maskman_meteor").toBuilder()
                .ammo(BulletAmmo.legacyItem("coin_maskman"))
                .ballistics(1.0F, DEFAULT_SPREAD, 10, 1, 1)
                .physics(0.1D, 300)
                .damage(20.0F, 30.0F)
                .blockDamage(false)
                .incendiaryTicks(3)
                .explosive(2.5F)
                .appearance(BulletStyle.METEOR, 0, BulletPlink.GRENADE, "smoke")
                .behavior(BulletBehaviorTag.MASKMAN_METEOR_FLAME_PARTICLES)
                .build();
    }

    private static BulletConfig wormBolt() {
        return standardBullet("worm_bolt").toBuilder()
                .ammo(BulletAmmo.legacyItem("coin_worm"))
                .ballistics(5.0F, 0.0F, 10, 1, 1)
                .physics(0.0D, 60)
                .damage(15.0F, 25.0F)
                .leadChance(0)
                .ricochet(false, 5.0D, 95, 2, 0.8D)
                .setToBolt(BulletTrail.WORM)
                .appearance(BulletStyle.BOLT, BulletTrail.WORM.legacyId(), BulletPlink.BULLET, "")
                .damageType(ModDamageSources.LASER)
                .build();
    }

    private static BulletConfig wormHeadBolt() {
        return standardBullet("worm_laser").toBuilder()
                .ammo(BulletAmmo.legacyItem("coin_worm"))
                .ballistics(5.0F, 0.0F, 10, 1, 1)
                .physics(0.0D, 100)
                .damage(35.0F, 60.0F)
                .leadChance(0)
                .ricochet(false, 5.0D, 95, 2, 0.8D)
                .setToBolt(BulletTrail.LASER)
                .appearance(BulletStyle.BOLT, BulletTrail.LASER.legacyId(), BulletPlink.BULLET, "")
                .damageType(ModDamageSources.LASER)
                .build();
    }

    private static BulletConfig rocketConfig(String legacyName) {
        return standardRocket(legacyName).toBuilder()
                .ammo(BulletAmmo.NOTHING)
                .damage(10.0F, 15.0F)
                .explosive(4.0F)
                .appearance(BulletStyle.ROCKET, 0, BulletPlink.GRENADE, "smoke")
                .build();
    }

    private static BulletConfig rocketUfo() {
        return rocketConfig("ufo_rocket").toBuilder()
                .explosive(0.0F)
                .destroysBlocks(false)
                .appearance(BulletStyle.ROCKET, 0, BulletPlink.GRENADE, "reddust")
                .behavior(BulletBehaviorTag.UFO_HOMING)
                .behavior(BulletBehaviorTag.UFO_BLAST)
                .build();
    }

    private LegacyBulletConfigs() {
    }
}
