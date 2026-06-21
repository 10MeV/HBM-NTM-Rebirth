package com.hbm.config;

import java.util.HashMap;

/**
 * Legacy package facade for server config reads that have modern consumers.
 * The old hbmServer.json runtime config file is intentionally not restored;
 * persisted values belong to the modern Forge config.
 */
@Deprecated(forRemoval = false)
public final class ServerConfig extends RunningConfig {
    public static final HashMap<String, ConfigWrapper> configMap = new HashMap<>();

    public static final ConfigWrapper<Boolean> DAMAGE_COMPATIBILITY_MODE = wrap(false);
    public static final ConfigWrapper<Float> MINE_AP_DAMAGE = wrap(10.0F);
    public static final ConfigWrapper<Float> MINE_HE_DAMAGE = wrap(35.0F);
    public static final ConfigWrapper<Float> MINE_SHRAP_DAMAGE = wrap(7.5F);
    public static final ConfigWrapper<Float> MINE_NUKE_DAMAGE = wrap(100.0F);
    public static final ConfigWrapper<Float> MINE_NAVAL_DAMAGE = wrap(60.0F);
    public static final ConfigWrapper<Boolean> TAINT_TRAILS = wrap(false,
            com.hbm.ntm.config.ServerConfig::taintTrailsEnabled,
            value -> setBoolean(com.hbm.ntm.config.ServerConfig.TAINT_TRAILS, value));
    public static final ConfigWrapper<Boolean> CRATE_OPEN_HELD = wrap(true);
    public static final ConfigWrapper<Boolean> CRATE_KEEP_CONTENTS = wrap(true);
    public static final ConfigWrapper<Integer> ITEM_HAZARD_DROP_TICKRATE = wrap(2,
            com.hbm.ntm.config.ServerConfig::droppedItemHazardTickRate,
            value -> setInt(com.hbm.ntm.config.ServerConfig.ITEM_HAZARD_DROP_TICKRATE, value));
    public static final ConfigWrapper<Boolean> ENABLE_MKU = wrap(true,
            com.hbm.ntm.config.ServerConfig::mkuEnabled,
            value -> setBoolean(com.hbm.ntm.config.ServerConfig.ENABLE_MKU, value));
    public static final ConfigWrapper<Boolean> STRUCTURE_DEBUG = wrap(false);
    public static final ConfigWrapper<Integer> AUTOCAL_MAX_CLOCK = wrap(20,
            com.hbm.ntm.config.ServerConfig::autocalMaxClockSpeed,
            value -> setInt(com.hbm.ntm.config.ServerConfig.AUTOCAL_MAX_CLOCK, value));

    static {
        registerDefaults();
    }

    public static void syncFromModern() {
        for (ConfigWrapper wrapper : configMap.values()) {
            wrapper.get();
        }
    }

    public static void initConfig() {
        syncFromModern();
    }

    public static void refresh() {
        syncFromModern();
    }

    public static void reload() {
        syncFromModern();
    }

    public static boolean mkuEnabled() {
        return ENABLE_MKU.get();
    }

    public static int itemHazardDropTickrate() {
        return Math.max(1, ITEM_HAZARD_DROP_TICKRATE.get());
    }

    public static boolean taintTrails() {
        return TAINT_TRAILS.get();
    }

    public static int autocalMaxClock() {
        return Math.max(1, AUTOCAL_MAX_CLOCK.get());
    }

    private static void registerDefaults() {
        configMap.put("DAMAGE_COMPATIBILITY_MODE", DAMAGE_COMPATIBILITY_MODE);
        configMap.put("MINE_AP_DAMAGE", MINE_AP_DAMAGE);
        configMap.put("MINE_HE_DAMAGE", MINE_HE_DAMAGE);
        configMap.put("MINE_SHRAP_DAMAGE", MINE_SHRAP_DAMAGE);
        configMap.put("MINE_NUKE_DAMAGE", MINE_NUKE_DAMAGE);
        configMap.put("MINE_NAVAL_DAMAGE", MINE_NAVAL_DAMAGE);
        configMap.put("TAINT_TRAILS", TAINT_TRAILS);
        configMap.put("CRATE_OPEN_HELD", CRATE_OPEN_HELD);
        configMap.put("CRATE_KEEP_CONTENTS", CRATE_KEEP_CONTENTS);
        configMap.put("ITEM_HAZARD_DROP_TICKRATE", ITEM_HAZARD_DROP_TICKRATE);
        configMap.put("ENABLE_MKU", ENABLE_MKU);
        configMap.put("STRUCTURE_DEBUG", STRUCTURE_DEBUG);
        configMap.put("AUTOCAL_MAX_CLOCK", AUTOCAL_MAX_CLOCK);
    }

    private static <T> ConfigWrapper<T> wrap(T fallback) {
        return new ConfigWrapper<>(fallback);
    }

    private static <T> ConfigWrapper<T> wrap(T fallback, java.util.function.Supplier<T> getter,
                                            java.util.function.Consumer<T> setter) {
        return new ConfigWrapper<>(fallback, getter, setter);
    }

    private static void setBoolean(net.minecraftforge.common.ForgeConfigSpec.BooleanValue value, boolean flag) {
        try {
            if (value != null) {
                value.set(flag);
            }
        } catch (IllegalStateException ignored) {
            // Keep facade value until Forge config is available.
        }
    }

    private static void setInt(net.minecraftforge.common.ForgeConfigSpec.IntValue value, int number) {
        try {
            if (value != null) {
                value.set(number);
            }
        } catch (IllegalStateException ignored) {
            // Keep facade value until Forge config is available.
        }
    }

    private ServerConfig() {
    }
}
