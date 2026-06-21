package com.hbm.ntm.explosion;

import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.entity.effect.CloudFleijaEntity;
import com.hbm.ntm.entity.effect.CloudFleijaRainbowEntity;
import com.hbm.ntm.entity.effect.CloudSoliniumEntity;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class NuclearExplosionUtil {
    public static boolean spawnNuclearCore(Level level, int radius, double x, double y, double z) {
        return add(level, NukeExplosionMk5Entity.statFac(level, radius, x, y, z));
    }

    public static boolean spawnNuclearCoreLoaded(Level level, int radius, double x, double y, double z) {
        return addLoaded(level, NukeExplosionMk5Entity.statFac(level, radius, x, y, z));
    }

    public static boolean spawnNuclear(Level level, int radius, double x, double y, double z) {
        boolean spawned = spawnNuclearCore(level, radius, x, y, z);
        if (spawned) {
            add(level, NukeTorexEntity.createStandard(level, x, y, z, radius));
        }
        return spawned;
    }

    private static boolean spawnNuclearLoaded(Level level, int radius, double x, double y, double z) {
        boolean spawned = spawnNuclearCoreLoaded(level, radius, x, y, z);
        if (spawned) {
            add(level, NukeTorexEntity.createStandard(level, x, y, z, radius));
        }
        return spawned;
    }

    public static boolean spawnNuclearNoFallout(Level level, int radius, double x, double y, double z) {
        return spawnNuclearNoFallout(level, radius, x, y, z, x, y, z);
    }

    public static boolean spawnNuclearNoFallout(Level level, int radius, double x, double y, double z,
            double cloudX, double cloudY, double cloudZ) {
        boolean spawned = add(level, NukeExplosionMk5Entity.statFacNoRad(level, radius, x, y, z));
        if (spawned) {
            add(level, NukeTorexEntity.createStandard(level, cloudX, cloudY, cloudZ, radius));
        }
        return spawned;
    }

    public static boolean spawnNuclearWithFallout(Level level, int radius, double x, double y, double z, int fallout) {
        return spawnNuclearWithFallout(level, radius, x, y, z, fallout, x, y, z);
    }

    public static boolean spawnNuclearWithFallout(Level level, int radius, double x, double y, double z, int fallout,
            double cloudX, double cloudY, double cloudZ) {
        boolean spawned = add(level, NukeExplosionMk5Entity.statFac(level, radius, x, y, z).moreFallout(fallout));
        if (spawned) {
            add(level, NukeTorexEntity.createStandard(level, cloudX, cloudY, cloudZ, radius));
        }
        return spawned;
    }

    private static boolean spawnNuclearWithFalloutLoaded(Level level, int radius, double x, double y, double z,
            int fallout) {
        boolean spawned = addLoaded(level, NukeExplosionMk5Entity.statFac(level, radius, x, y, z).moreFallout(fallout));
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

    public static boolean spawnCustomEuphemium(Level level, double x, double y, double z) {
        boolean spawned = add(level, NukeExplosionMk3Entity.createFleija(level, x, y, z, 150));
        add(level, CloudFleijaRainbowEntity.create(level, x, y, z, 50));
        return spawned;
    }

    public static boolean spawnAntiSchrabidium(Level level, double x, double y, double z) {
        return spawnFleija(level, x, y, z, antiSchrabRadius());
    }

    public static boolean spawnGadget(Level level, double x, double y, double z) {
        return spawnNuclear(level, gadgetRadius(), x, y, z);
    }

    public static boolean spawnBoy(Level level, double x, double y, double z) {
        boolean spawned = spawnNuclearCore(level, boyRadius(), x, y, z);
        if (spawned) {
            NukeTorexEntity torex = new NukeTorexEntity(level).setScale(1.5F);
            torex.setType(0);
            torex.setPos(x, y + 0.5D, z);
            add(level, torex);
        }
        return spawned;
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
        return spawnNuclearLoaded(level, missileRadius(), x, y, z);
    }

    public static boolean spawnMissileMirv(Level level, double x, double y, double z) {
        return spawnNuclearLoaded(level, missileRadius() * 2, x, y, z);
    }

    public static boolean spawnMissileDoomsday(Level level, double x, double y, double z) {
        return spawnNuclearWithFalloutLoaded(level, missileRadius() * 2, x, y, z, 100);
    }

    public static boolean spawnMissileDoomsdayRusted(Level level, double x, double y, double z) {
        return spawnNuclearWithFalloutLoaded(level, missileRadius(), x, y, z, 100);
    }

    public static void explodeFatman(Level level, double x, double y, double z) {
        ExplosionNukeSmall.explodeConfiguredHigh(level, x, y, z);
    }

    public static void explodeNuka(Level level, double x, double y, double z) {
        ExplosionNukeSmall.explode(level, x, y, z, ExplosionNukeSmall.PARAMS_MEDIUM.copy().blastRadius(nukaRadius()));
    }

    public static int gadgetRadius() {
        return BombConfig.gadgetRadius();
    }

    public static int boyRadius() {
        return BombConfig.boyRadius();
    }

    public static int manRadius() {
        return BombConfig.manRadius();
    }

    public static int mikeRadius() {
        return BombConfig.mikeRadius();
    }

    public static int tsarRadius() {
        return BombConfig.tsarRadius();
    }

    public static int prototypeRadius() {
        return BombConfig.prototypeRadius();
    }

    public static int fleijaRadius() {
        return BombConfig.fleijaRadius();
    }

    public static int soliniumRadius() {
        return BombConfig.soliniumRadius();
    }

    public static int n2Radius() {
        return BombConfig.n2Radius();
    }

    public static int missileRadius() {
        return BombConfig.missileRadius();
    }

    public static int mirvRadius() {
        return BombConfig.mirvRadius();
    }

    public static int fatmanRadius() {
        return BombConfig.fatmanRadius();
    }

    public static int nukaRadius() {
        return BombConfig.nukaRadius();
    }

    public static int antiSchrabRadius() {
        return BombConfig.antiSchrabidiumRadius();
    }

    private static boolean add(Level level, Entity entity) {
        if (level == null || level.isClientSide() || entity == null || entity.isRemoved()) {
            return false;
        }
        return level.addFreshEntity(entity);
    }

    private static boolean addLoaded(Level level, Entity entity) {
        if (level == null || level.isClientSide() || entity == null || entity.isRemoved()) {
            return false;
        }
        return WorldUtil.loadAndSpawnEntityInWorld(entity);
    }

    private NuclearExplosionUtil() {
    }
}
