package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class HbmCommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue LOG_STARTUP;
    public static final ForgeConfigSpec.BooleanValue ENABLE_LEGACY_ID_NOTES;

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

        NetworkConfig.define(builder);
        RadiationConfig.define(builder);
        BombConfig.define(builder);
        WeaponConfig.define(builder);

        SPEC = builder.build();
    }

    private HbmCommonConfig() {
    }
}
