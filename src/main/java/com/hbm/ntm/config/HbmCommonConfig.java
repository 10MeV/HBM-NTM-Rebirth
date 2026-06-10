package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HbmCommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue LOG_STARTUP;
    public static final ForgeConfigSpec.BooleanValue ENABLE_LEGACY_ID_NOTES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTENDED_LOGGING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CRYSTAL_VIRUS_SPREADING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MOTD;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("migration");
        LOG_STARTUP = builder
                .comment("Log scaffold startup information during common setup.")
                .define("logStartup", true);
        ENABLE_LEGACY_ID_NOTES = builder
                .comment("Keep explicit legacy ID notes enabled while porting from 1.7.10.")
                .define("enableLegacyIdNotes", true);
        builder.pop();

        builder.push("general");
        ENABLE_EXTENDED_LOGGING = builder
                .comment("Legacy GeneralConfig 1.18_enableExtendedLogging: logs detonators, nuclear explosions, missile launches, grenades, and related high-impact actions.")
                .define("enableExtendedLogging", false);
        ENABLE_CRYSTAL_VIRUS_SPREADING = builder
                .comment("Legacy GeneralConfig 1.21_enableVirus: allows crystal_virus blocks to spread into adjacent solid blocks and then harden.")
                .define("enableCrystalVirusSpreading", false);
        ENABLE_MOTD = builder
                .comment("Legacy GeneralConfig 1.36_enableMOTD: shows the HBM loaded-world chat message when a player joins.")
                .define("enableMotd", true);
        builder.pop();

        NetworkConfig.define(builder);
        RadiationConfig.define(builder);
        ServerConfig.define(builder);
        BombConfig.define(builder);
        WeaponConfig.define(builder);
        ToolConfig.define(builder);
        PotionConfig.define(builder);
        AshpitConfig.define(builder);
        SteamEngineConfig.define(builder);
        CoolingTowerConfig.define(builder);
        SteamTurbineConfig.define(builder);
        OilDrillConfig.define(builder);
        BoilerConfig.define(builder);
        RadarConfig.define(builder);

        SPEC = builder.build();
    }

    public static boolean startupLoggingEnabled() {
        return booleanValue(LOG_STARTUP, true);
    }

    public static boolean legacyIdNotesEnabled() {
        return booleanValue(ENABLE_LEGACY_ID_NOTES, true);
    }

    public static boolean extendedLoggingEnabled() {
        return booleanValue(ENABLE_EXTENDED_LOGGING, false);
    }

    public static boolean crystalVirusSpreadingEnabled() {
        return booleanValue(ENABLE_CRYSTAL_VIRUS_SPREADING, false);
    }

    public static boolean motdEnabled() {
        return booleanValue(ENABLE_MOTD, true);
    }

    private static boolean booleanValue(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private HbmCommonConfig() {
    }
}
