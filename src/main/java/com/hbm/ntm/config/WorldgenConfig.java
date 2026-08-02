package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Source-backed controls for ordinary, non-event world generation. */
public final class WorldgenConfig {
    public static ForgeConfigSpec.IntValue METEORITE_SPAWN_CHUNKS;
    public static ForgeConfigSpec.IntValue GAS_BUBBLE_SPAWN_CHUNKS;
    public static ForgeConfigSpec.IntValue EXPLOSIVE_GAS_BUBBLE_SPAWN_CHUNKS;
    public static ForgeConfigSpec.BooleanValue RADIATION_HOTSPOTS_ENABLED;
    public static ForgeConfigSpec.IntValue RADIATION_HOTSPOT_SPAWN_CHUNKS;
    public static ForgeConfigSpec.IntValue LEGACY_SURFACE_FIXTURE_MAP_FEATURES_MODE;
    public static ForgeConfigSpec.BooleanValue LANDMINE_SPAWNING_ENABLED;
    public static ForgeConfigSpec.IntValue LANDMINE_SPAWN_CHUNKS;
    public static ForgeConfigSpec.IntValue BROADCASTER_SPAWN_CHUNKS;
    public static ForgeConfigSpec.IntValue CHLORINE_GEYSER_SPAWN_CHUNKS;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("worldgen");
        METEORITE_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig 2.23_meteoriteSpawnRate: average generated chunks per ground-refresh meteorite. Set to 0 to disable. This does not control meteor fall/impact events, which are excluded.")
                .defineInRange("meteoriteSpawnChunks", 200, 0, Integer.MAX_VALUE);
        GAS_BUBBLE_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig 2.17_gasBubbleSpawnRate: average generated chunks per flammable gas bubble. Set to 0 to disable.")
                .defineInRange("gasBubbleSpawnChunks", 12, 0, Integer.MAX_VALUE);
        EXPLOSIVE_GAS_BUBBLE_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig 2.19_explosiveBubbleSpawnRate: average generated chunks per explosive gas bubble. Set to 0 to disable.")
                .defineInRange("explosiveGasBubbleSpawnChunks", 0, 0, Integer.MAX_VALUE);
        RADIATION_HOTSPOTS_ENABLED = builder
                .comment("Legacy GeneralConfig 1.06_enableRadHotspotSpawn: allows natural Sellafield desert hotspots to generate.")
                .define("radiationHotspotsEnabled", true);
        RADIATION_HOTSPOT_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig 4.15_radHotspotSpawn: average generated chunks per natural Sellafield desert hotspot. Set to 0 to disable.")
                .defineInRange("radiationHotspotSpawnChunks", 5000, 0, Integer.MAX_VALUE);
        LEGACY_SURFACE_FIXTURE_MAP_FEATURES_MODE = builder
                .comment("Legacy GeneralConfig 1.03_enableDungeonSpawn contract for ordinary surface fixtures: 0=false, 1=true, 2=respect the world's Generate Structures option. This does not enable excluded ruins, dungeons, red rooms, or reward structures.")
                .defineInRange("legacySurfaceFixtureMapFeaturesMode", 2, 0, 2);
        LANDMINE_SPAWNING_ENABLED = builder
                .comment("Legacy GeneralConfig 1.05_enableLandmineSpawn: allows naturally generated AP landmines.")
                .define("landmineSpawningEnabled", true);
        LANDMINE_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig minefreq: average generated chunks per natural AP landmine. Set to 0 to disable.")
                .defineInRange("landmineSpawnChunks", 64, 0, Integer.MAX_VALUE);
        BROADCASTER_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig broadcaster: average generated chunks per corrupted broadcaster. Set to 0 to disable.")
                .defineInRange("broadcasterSpawnChunks", 5000, 0, Integer.MAX_VALUE);
        CHLORINE_GEYSER_SPAWN_CHUNKS = builder
                .comment("Legacy WorldConfig geyserChlorine: average plains chunks per chlorine geyser template. Set to 0 to disable.")
                .defineInRange("chlorineGeyserSpawnChunks", 3000, 0, Integer.MAX_VALUE);
        builder.pop();
    }

    public static int meteoriteSpawnChunks() {
        return METEORITE_SPAWN_CHUNKS.get();
    }

    public static int gasBubbleSpawnChunks() {
        return GAS_BUBBLE_SPAWN_CHUNKS.get();
    }

    public static int explosiveGasBubbleSpawnChunks() {
        return EXPLOSIVE_GAS_BUBBLE_SPAWN_CHUNKS.get();
    }

    public static boolean radiationHotspotsEnabled() {
        return RADIATION_HOTSPOTS_ENABLED.get();
    }

    public static int radiationHotspotSpawnChunks() {
        return RADIATION_HOTSPOT_SPAWN_CHUNKS.get();
    }

    public static int legacySurfaceFixtureMapFeaturesMode() {
        return LEGACY_SURFACE_FIXTURE_MAP_FEATURES_MODE.get();
    }

    public static boolean landmineSpawningEnabled() {
        return LANDMINE_SPAWNING_ENABLED.get();
    }

    public static int landmineSpawnChunks() {
        return LANDMINE_SPAWN_CHUNKS.get();
    }

    public static int broadcasterSpawnChunks() {
        return BROADCASTER_SPAWN_CHUNKS.get();
    }

    public static int chlorineGeyserSpawnChunks() {
        return CHLORINE_GEYSER_SPAWN_CHUNKS.get();
    }

    private WorldgenConfig() {
    }
}
