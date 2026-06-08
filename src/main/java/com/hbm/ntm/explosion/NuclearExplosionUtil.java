package com.hbm.ntm.explosion;

import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.entity.effect.CloudFleijaEntity;
import com.hbm.ntm.entity.effect.CloudFleijaRainbowEntity;
import com.hbm.ntm.entity.effect.CloudSoliniumEntity;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

public final class NuclearExplosionUtil {
    public static boolean spawnNuclear(Level level, int radius, double x, double y, double z) {
        boolean spawned = add(level, NukeExplosionMk5Entity.statFac(level, radius, x, y, z));
        if (spawned) {
            add(level, NukeTorexEntity.createStandard(level, x, y, z, radius));
        }
        return spawned;
    }

    public static boolean spawnNuclearNoFallout(Level level, int radius, double x, double y, double z) {
        boolean spawned = add(level, NukeExplosionMk5Entity.statFacNoRad(level, radius, x, y, z));
        if (spawned) {
            add(level, NukeTorexEntity.createStandard(level, x, y, z, radius));
        }
        return spawned;
    }

    public static boolean spawnNuclearWithFallout(Level level, int radius, double x, double y, double z, int fallout) {
        boolean spawned = add(level, NukeExplosionMk5Entity.statFac(level, radius, x, y, z).moreFallout(fallout));
        if (spawned) {
            add(level, NukeTorexEntity.createStandard(level, x, y, z, radius));
        }
        return spawned;
    }

    public static boolean spawnBalefireCloud(Level level, int radius, double x, double y, double z) {
        return add(level, NukeTorexEntity.createBalefire(level, x, y, z, radius));
    }

    public static boolean spawnFleija(Level level, double x, double y, double z, int radius) {
        return spawnFleija(level, x, y, z, radius, x, y, z);
    }

    private static boolean spawnFleija(Level level, double x, double y, double z, int radius,
            double cloudX, double cloudY, double cloudZ) {
        boolean spawned = add(level, NukeExplosionMk3Entity.statFacFleija(level, x, y, z, radius));
        if (spawned) {
            add(level, CloudFleijaEntity.create(level, cloudX, cloudY, cloudZ, radius));
        }
        return spawned;
    }

    public static boolean spawnSolinium(Level level, double x, double y, double z, int radius) {
        return spawnSolinium(level, x, y, z, radius, x, y, z);
    }

    private static boolean spawnSolinium(Level level, double x, double y, double z, int radius,
            double cloudX, double cloudY, double cloudZ) {
        boolean spawned = add(level, NukeExplosionMk3Entity.createSolinium(level, x, y, z, radius));
        if (spawned) {
            add(level, CloudSoliniumEntity.create(level, cloudX, cloudY, cloudZ, radius));
        }
        return spawned;
    }

    public static boolean spawnFleijaRainbow(Level level, double x, double y, double z, int radius, int cloudAge) {
        boolean spawned = add(level, NukeExplosionMk3Entity.statFacFleija(level, x, y, z, radius));
        if (spawned) {
            add(level, CloudFleijaRainbowEntity.create(level, x, y, z, cloudAge));
        }
        return spawned;
    }

    public static boolean spawnAntiSchrabidium(Level level, double x, double y, double z) {
        return spawnFleija(level, x, y, z, antiSchrabRadius());
    }

    public static boolean spawnGadget(Level level, double x, double y, double z) {
        return spawnNuclear(level, gadgetRadius(), x, y, z);
    }

    public static boolean spawnBoy(Level level, double x, double y, double z) {
        return spawnNuclear(level, boyRadius(), x, y, z);
    }

    public static boolean spawnMan(Level level, double x, double y, double z) {
        return spawnNuclear(level, manRadius(), x, y, z);
    }

    public static boolean spawnMike(Level level, double x, double y, double z) {
        return spawnNuclear(level, mikeRadius(), x, y, z);
    }

    public static boolean spawnTsar(Level level, double x, double y, double z) {
        return spawnNuclear(level, tsarRadius(), x, y, z);
    }

    public static boolean spawnPrototype(Level level, double x, double y, double z) {
        return spawnFleija(level, x, y, z, prototypeRadius(), x - 0.5D, y - 0.5D, z - 0.5D);
    }

    public static boolean spawnFleijaBomb(Level level, double x, double y, double z) {
        return spawnFleija(level, x, y, z, fleijaRadius(), x - 0.5D, y - 0.5D, z - 0.5D);
    }

    public static boolean spawnSoliniumBomb(Level level, double x, double y, double z) {
        return spawnSolinium(level, x, y, z, soliniumRadius(), x - 0.5D, y - 0.5D, z - 0.5D);
    }

    public static boolean spawnN2(Level level, double x, double y, double z) {
        return spawnNuclearNoFallout(level, n2Radius(), x, y, z);
    }

    public static boolean spawnMissileNuclear(Level level, double x, double y, double z) {
        return spawnNuclear(level, missileRadius(), x, y, z);
    }

    public static boolean spawnMissileMirv(Level level, double x, double y, double z) {
        return spawnNuclear(level, missileRadius() * 2, x, y, z);
    }

    public static boolean spawnMissileDoomsday(Level level, double x, double y, double z) {
        return spawnNuclearWithFallout(level, missileRadius() * 2, x, y, z, 100);
    }

    public static boolean spawnMissileDoomsdayRusted(Level level, double x, double y, double z) {
        return spawnNuclearWithFallout(level, missileRadius(), x, y, z, 100);
    }

    public static void explodeFatman(Level level, double x, double y, double z) {
        ExplosionNukeSmall.explodeConfiguredHigh(level, x, y, z);
    }

    public static void explodeNuka(Level level, double x, double y, double z) {
        ExplosionNukeSmall.explode(level, x, y, z, ExplosionNukeSmall.PARAMS_MEDIUM.copy().blastRadius(nukaRadius()));
    }

    public static int gadgetRadius() {
        return configRadius(BombConfig.GADGET_RADIUS, BombConfig.GADGET_RADIUS_DEFAULT);
    }

    public static int boyRadius() {
        return configRadius(BombConfig.BOY_RADIUS, BombConfig.BOY_RADIUS_DEFAULT);
    }

    public static int manRadius() {
        return configRadius(BombConfig.MAN_RADIUS, BombConfig.MAN_RADIUS_DEFAULT);
    }

    public static int mikeRadius() {
        return configRadius(BombConfig.MIKE_RADIUS, BombConfig.MIKE_RADIUS_DEFAULT);
    }

    public static int tsarRadius() {
        return configRadius(BombConfig.TSAR_RADIUS, BombConfig.TSAR_RADIUS_DEFAULT);
    }

    public static int prototypeRadius() {
        return configRadius(BombConfig.PROTOTYPE_RADIUS, BombConfig.PROTOTYPE_RADIUS_DEFAULT);
    }

    public static int fleijaRadius() {
        return configRadius(BombConfig.FLEIJA_RADIUS, BombConfig.FLEIJA_RADIUS_DEFAULT);
    }

    public static int soliniumRadius() {
        return configRadius(BombConfig.SOLINIUM_RADIUS, BombConfig.SOLINIUM_RADIUS_DEFAULT);
    }

    public static int n2Radius() {
        return configRadius(BombConfig.N2_RADIUS, BombConfig.N2_RADIUS_DEFAULT);
    }

    public static int missileRadius() {
        return configRadius(BombConfig.MISSILE_RADIUS, BombConfig.MISSILE_RADIUS_DEFAULT);
    }

    public static int mirvRadius() {
        return configRadius(BombConfig.MIRV_RADIUS, BombConfig.MIRV_RADIUS_DEFAULT);
    }

    public static int fatmanRadius() {
        return configRadius(BombConfig.FATMAN_RADIUS, BombConfig.FATMAN_RADIUS_DEFAULT);
    }

    public static int nukaRadius() {
        return configRadius(BombConfig.NUKA_RADIUS, BombConfig.NUKA_RADIUS_DEFAULT);
    }

    public static int antiSchrabRadius() {
        return configRadius(BombConfig.A_SCHRAB_RADIUS, BombConfig.A_SCHRAB_RADIUS_DEFAULT);
    }

    private static boolean add(Level level, Entity entity) {
        if (level == null || level.isClientSide() || entity == null || entity.isRemoved()) {
            return false;
        }
        return level.addFreshEntity(entity);
    }

    private static int configRadius(ForgeConfigSpec.IntValue value, int fallback) {
        return value == null ? fallback : value.get();
    }

    private NuclearExplosionUtil() {
    }
}
