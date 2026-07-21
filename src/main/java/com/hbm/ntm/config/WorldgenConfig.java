package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Source-backed controls for ordinary, non-event world generation. */
public final class WorldgenConfig {
    public static ForgeConfigSpec.IntValue METEORITE_SPAWN_CHUNKS;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("worldgen");
        METEORITE_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig 2.23_meteoriteSpawnRate: average generated chunks per ground-refresh meteorite. Set to 0 to disable. This does not control meteor fall/impact events, which are excluded.")
                .defineInRange("meteoriteSpawnChunks", 200, 0, Integer.MAX_VALUE);
        builder.pop();
    }

    public static int meteoriteSpawnChunks() {
        return METEORITE_SPAWN_CHUNKS.get();
    }

    private WorldgenConfig() {
    }
}
