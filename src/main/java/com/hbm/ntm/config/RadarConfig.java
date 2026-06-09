package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class RadarConfig {
    public static final long POWER_CAP_DEFAULT = 100_000L;
    public static final long CONSUMPTION_DEFAULT = 500L;
    public static final int RADAR_RANGE_DEFAULT = 1_000;
    public static final int RADAR_LARGE_RANGE_DEFAULT = 3_000;
    public static final int RADAR_BUFFER_DEFAULT = 30;
    public static final int RADAR_ALTITUDE_DEFAULT = 55;
    public static final int MAP_CHUNK_LOAD_CAP_DEFAULT = 10;
    public static final boolean MAP_GENERATE_CHUNKS_DEFAULT = false;

    public static ForgeConfigSpec.LongValue POWER_CAP;
    public static ForgeConfigSpec.LongValue CONSUMPTION;
    public static ForgeConfigSpec.IntValue RADAR_RANGE;
    public static ForgeConfigSpec.IntValue RADAR_LARGE_RANGE;
    public static ForgeConfigSpec.IntValue RADAR_BUFFER;
    public static ForgeConfigSpec.IntValue RADAR_ALTITUDE;
    public static ForgeConfigSpec.IntValue MAP_CHUNK_LOAD_CAP;
    public static ForgeConfigSpec.BooleanValue MAP_GENERATE_CHUNKS;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        builder.push("radar");
        POWER_CAP = builder
                .comment("Legacy radar L:powerCap: internal energy capacity and receive limit.")
                .defineInRange("powerCap", POWER_CAP_DEFAULT, 0L, Long.MAX_VALUE);
        CONSUMPTION = builder
                .comment("Legacy radar L:consumption: HE consumed per scan tick.")
                .defineInRange("consumption", CONSUMPTION_DEFAULT, 0L, Long.MAX_VALUE);
        RADAR_RANGE = builder
                .comment("Legacy radar I:radarRange: ordinary radar horizontal scan half-range in blocks.")
                .defineInRange("radarRange", RADAR_RANGE_DEFAULT, 0, 100_000);
        RADAR_LARGE_RANGE = builder
                .comment("Legacy radar_large I:radarLargeRange: large radar horizontal scan half-range in blocks.")
                .defineInRange("radarLargeRange", RADAR_LARGE_RANGE_DEFAULT, 0, 100_000);
        RADAR_BUFFER = builder
                .comment("Legacy radar I:radarBuffer: target must be this many blocks above the radar.")
                .defineInRange("radarBuffer", RADAR_BUFFER_DEFAULT, 0, 1_000);
        RADAR_ALTITUDE = builder
                .comment("Legacy radar I:radarAltitude: minimum radar block Y required for scanning.")
                .defineInRange("radarAltitude", RADAR_ALTITUDE_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);
        MAP_CHUNK_LOAD_CAP = builder
                .comment("Legacy radar I:chunkLoadCap: maximum unloaded map chunks the radar may attempt per tick while showMap is enabled.")
                .defineInRange("mapChunkLoadCap", MAP_CHUNK_LOAD_CAP_DEFAULT, 0, 100);
        MAP_GENERATE_CHUNKS = builder
                .comment("Legacy radar B:generateChunks: allows radar map scanning to generate/load missing chunks. Default false preserves the old safe default.")
                .define("mapGenerateChunks", MAP_GENERATE_CHUNKS_DEFAULT);
        builder.pop();
        builder.pop();
    }

    public static long powerCap() {
        return POWER_CAP == null ? POWER_CAP_DEFAULT : Math.max(0L, POWER_CAP.get());
    }

    public static long consumption() {
        return CONSUMPTION == null ? CONSUMPTION_DEFAULT : Math.max(0L, CONSUMPTION.get());
    }

    public static int radarRange() {
        return RADAR_RANGE == null ? RADAR_RANGE_DEFAULT : Math.max(0, RADAR_RANGE.get());
    }

    public static int radarLargeRange() {
        return RADAR_LARGE_RANGE == null ? RADAR_LARGE_RANGE_DEFAULT : Math.max(0, RADAR_LARGE_RANGE.get());
    }

    public static int radarBuffer() {
        return RADAR_BUFFER == null ? RADAR_BUFFER_DEFAULT : Math.max(0, RADAR_BUFFER.get());
    }

    public static int radarAltitude() {
        return RADAR_ALTITUDE == null ? RADAR_ALTITUDE_DEFAULT : RADAR_ALTITUDE.get();
    }

    public static int mapChunkLoadCap() {
        return MAP_CHUNK_LOAD_CAP == null ? MAP_CHUNK_LOAD_CAP_DEFAULT : Math.max(0, MAP_CHUNK_LOAD_CAP.get());
    }

    public static boolean mapGenerateChunks() {
        return MAP_GENERATE_CHUNKS != null && MAP_GENERATE_CHUNKS.get();
    }

    private RadarConfig() {
    }
}
