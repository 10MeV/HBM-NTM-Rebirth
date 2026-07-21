package com.hbm.ntm.bullet;

import com.hbm.ntm.radiation.ModDamageSources;

public final class LegacyBulletConfigs {
    public static final float DEFAULT_SPREAD = 0.005F;

    public static final BulletConfig TURBINE = turbine();
    public static final BulletConfig MASKMAN_BOLT = maskmanBolt();

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
                .ballistics(base.velocity() * 0.3F, base.spread(), base.wear() * 0.5F,
                        base.bulletsMin(), base.bulletsMax())
                .damage(base.damageMin() * 2.0F, base.damageMax() * 2.0F)
                .ricochet(false, base.ricochetAngle(), base.lowerBoundRicochetChance(),
                        base.higherBoundRicochetChance(), base.bounceModifier())
                .penetration(true)
                .appearance(base.style(), base.trail(), base.plink(), "greendust")
                .spentCasingName(chlorophyteCasingName(base.spentCasingName()))
                .behavior(BulletBehaviorTag.CHLOROPHYTE_HOMING)
                .behavior(BulletBehaviorTag.PENETRATION_HOMING_RESET)
                .build();
    }

    private static String chlorophyteCasingName(String baseName) {
        return baseName == null || baseName.isBlank() ? "" : baseName + "Cl";
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

    private LegacyBulletConfigs() {
    }
}
