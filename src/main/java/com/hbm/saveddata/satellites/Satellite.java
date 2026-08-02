package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Satellite extends com.hbm.ntm.satellite.Satellite {
    public static final List<Class<? extends Satellite>> satellites = new ArrayList<>();
    public static final HashMap<Item, Class<? extends Satellite>> itemToClass = new HashMap<>();

    static {
        registerDefaults();
    }

    public static void register() {
        registerDefaults();
    }

    public static void registerSatellite(Class<? extends com.hbm.ntm.satellite.Satellite> satelliteClass, Item item) {
        if (satelliteClass != null && !Satellite.class.isAssignableFrom(satelliteClass)) {
            com.hbm.ntm.satellite.Satellite.registerSatellite(satelliteClass, item);
            return;
        }
        Class<? extends Satellite> legacyClass = satelliteClass == null
                ? null
                : satelliteClass.asSubclass(Satellite.class);
        if (!itemToClass.containsKey(item) && !itemToClass.containsValue(legacyClass)) {
            if (legacyClass == null || !isDefaultSatelliteClass(legacyClass) || !satellites.contains(legacyClass)) {
                satellites.add(legacyClass);
            }
            itemToClass.put(item, legacyClass);
            if (item != null && legacyClass != null) {
                com.hbm.ntm.satellite.Satellite.getTypeFromClass(legacyClass)
                        .ifPresent(type -> com.hbm.ntm.satellite.Satellite.registerSatelliteItem(item, type));
            }
        }
    }

    public static void registerSatellite(LegacySatelliteType type, Item item) {
        Class<? extends Satellite> satelliteClass = legacyClassFromType(type);
        if (satelliteClass != null) {
            registerSatellite(satelliteClass, item);
        }
    }

    public static Satellite create(int id) {
        try {
            return satellites.get(id).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean orbit(ServerLevel level, int id, int frequency, double x, double y, double z) {
        return com.hbm.ntm.satellite.Satellite.orbit(level, id, frequency, x, y, z);
    }

    public static boolean orbit(Level level, int id, int frequency, double x, double y, double z) {
        return com.hbm.ntm.satellite.Satellite.orbit(level, id, frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, Item item, int frequency, double x, double y, double z) {
        return com.hbm.ntm.satellite.Satellite.orbit(level, item, frequency, x, y, z);
    }

    public static boolean orbit(Level level, Item item, int frequency, double x, double y, double z) {
        return com.hbm.ntm.satellite.Satellite.orbit(level, item, frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, ItemStack stack, int frequency, double x, double y, double z) {
        return com.hbm.ntm.satellite.Satellite.orbit(level, stack, frequency, x, y, z);
    }

    public static boolean orbit(Level level, ItemStack stack, int frequency, double x, double y, double z) {
        return com.hbm.ntm.satellite.Satellite.orbit(level, stack, frequency, x, y, z);
    }

    public static int getIDFromItem(Item item) {
        Class<? extends Satellite> satelliteClass = itemToClass.get(item);
        return satellites.indexOf(satelliteClass);
    }

    public static int getIDFromStack(ItemStack stack) {
        return com.hbm.ntm.satellite.Satellite.getIDFromStack(stack);
    }

    @Override
    public int getID() {
        return satellites.indexOf(getClass());
    }

    private static void registerDefaults() {
        registerDefault(SatelliteMapper.class, LegacySatelliteType.MAPPER);
        registerDefault(SatelliteScanner.class, LegacySatelliteType.SCANNER);
        registerDefault(SatelliteRadar.class, LegacySatelliteType.RADAR);
        registerDefault(SatelliteLaser.class, LegacySatelliteType.LASER);
        registerDefault(SatelliteResonator.class, LegacySatelliteType.RESONATOR);
        registerDefault(SatelliteRelay.class, LegacySatelliteType.RELAY);
        registerDefault(SatelliteMiner.class, LegacySatelliteType.MINER);
        registerDefault(SatelliteLunarMiner.class, LegacySatelliteType.LUNAR_MINER);
        registerDefault(SatelliteHorizons.class, LegacySatelliteType.HORIZONS);
        registerDefault(SatellitePrecisionLaser.class, LegacySatelliteType.PRECISION_LASER);
        registerDefault(SatelliteDetector.class, LegacySatelliteType.DETECTOR);
        registerDefault(SatelliteRayScan.class, LegacySatelliteType.RAY_SCAN);
    }

    private static void registerDefault(Class<? extends Satellite> satelliteClass, LegacySatelliteType type) {
        if (!satellites.contains(satelliteClass)) {
            satellites.add(satelliteClass);
        }
        com.hbm.ntm.satellite.Satellite.registerSatelliteClass(satelliteClass, type);
    }

    private static boolean isDefaultSatelliteClass(Class<? extends Satellite> satelliteClass) {
        return satelliteClass == SatelliteMapper.class
                || satelliteClass == SatelliteScanner.class
                || satelliteClass == SatelliteRadar.class
                || satelliteClass == SatelliteLaser.class
                || satelliteClass == SatelliteResonator.class
                || satelliteClass == SatelliteRelay.class
                || satelliteClass == SatelliteMiner.class
                || satelliteClass == SatelliteLunarMiner.class
                || satelliteClass == SatelliteHorizons.class
                || satelliteClass == SatellitePrecisionLaser.class
                || satelliteClass == SatelliteDetector.class
                || satelliteClass == SatelliteRayScan.class;
    }

    private static Class<? extends Satellite> legacyClassFromType(LegacySatelliteType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case MAPPER -> SatelliteMapper.class;
            case SCANNER -> SatelliteScanner.class;
            case RADAR -> SatelliteRadar.class;
            case LASER -> SatelliteLaser.class;
            case RESONATOR -> SatelliteResonator.class;
            case RELAY -> SatelliteRelay.class;
            case MINER -> SatelliteMiner.class;
            case LUNAR_MINER -> SatelliteLunarMiner.class;
            case HORIZONS -> SatelliteHorizons.class;
            case PRECISION_LASER -> SatellitePrecisionLaser.class;
            case DETECTOR -> SatelliteDetector.class;
            case RAY_SCAN -> SatelliteRayScan.class;
        };
    }
}
