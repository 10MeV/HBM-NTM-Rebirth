package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HbmCommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue LOG_STARTUP;
    public static final ForgeConfigSpec.BooleanValue ENABLE_LEGACY_ID_NOTES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTENDED_LOGGING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CRYSTAL_VIRUS_SPREADING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MOTD;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MACHINE_GRAVITY;
    public static final ForgeConfigSpec.IntValue ICF_LASER_CAPACITOR_POWER;
    public static final ForgeConfigSpec.IntValue ICF_LASER_TURBO_POWER;
    public static final ForgeConfigSpec.LongValue FUSION_MHDT_MINIMUM_PLASMA;

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
        ENABLE_MACHINE_GRAVITY = builder
                .comment("Legacy GeneralConfig 1.44_enableMachineGravity: large machines require a proper foundation and tilt when unsupported. The old 528-only companion switch is intentionally not migrated.")
                .define("enableMachineGravity", false);
        builder.pop();

        builder.push("machines");
        builder.push("icfLaser");
        ICF_LASER_CAPACITOR_POWER = builder
                .comment("Legacy MachineDynConfig icfLaser I:capacitorPower: laser power contribution per sqrt(valid capacitors).")
                .defineInRange("capacitorPower", 2_500_000, 0, Integer.MAX_VALUE);
        ICF_LASER_TURBO_POWER = builder
                .comment("Legacy MachineDynConfig icfLaser I:turboPower: laser power contribution per sqrt(min(valid turbochargers, valid capacitors)).")
                .defineInRange("turboPower", 5_000_000, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.push("mhd-turbine");
        FUSION_MHDT_MINIMUM_PLASMA = builder
                .comment("Legacy MachineDynConfig mhd-turbine L:minimumPlasma: plasma threshold for full MHD turbine output.")
                .defineInRange("minimumPlasma", 5_000_000L, 0L, Long.MAX_VALUE);
        builder.pop();
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
        CondenserConfig.define(builder);
        PoweredCondenserConfig.define(builder);
        SteamTurbineConfig.define(builder);
        OilDrillConfig.define(builder);
        BoilerConfig.define(builder);
        RadarConfig.define(builder);
        RtgConfig.define(builder);

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

    public static boolean machineGravityEnabled() {
        return booleanValue(ENABLE_MACHINE_GRAVITY, false);
    }

    public static int icfLaserCapacitorPower() {
        return intValue(ICF_LASER_CAPACITOR_POWER, 2_500_000);
    }

    public static int icfLaserTurboPower() {
        return intValue(ICF_LASER_TURBO_POWER, 5_000_000);
    }

    public static long fusionMhdtMinimumPlasma() {
        return longValue(FUSION_MHDT_MINIMUM_PLASMA, 5_000_000L);
    }

    private static boolean booleanValue(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static int intValue(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static long longValue(ForgeConfigSpec.LongValue value, long fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private HbmCommonConfig() {
    }
}
