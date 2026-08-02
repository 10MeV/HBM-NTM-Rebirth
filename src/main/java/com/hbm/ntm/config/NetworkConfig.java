package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class NetworkConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_PACKET_THREADING;
    public static ForgeConfigSpec.BooleanValue PACKET_THREADING_ERROR_BYPASS;
    public static ForgeConfigSpec.IntValue PACKET_THREADING_WAIT_TIMEOUT_MS;
    public static ForgeConfigSpec.IntValue PACKET_THREADING_MAX_PENDING;
    public static ForgeConfigSpec.IntValue PACKET_THREADING_FALLBACK_CLEAR_THRESHOLD;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("network");
        builder.push("packetThreading");
        ENABLE_PACKET_THREADING = builder
                .comment("Legacy 0.01_enablePacketThreading key: prepare selected high-frequency S2C packets and defer their ordered sends to server tick end. Modern Forge channel sends remain on the server thread; no packet worker is created.")
                .define("enablePacketThreading", true);
        PACKET_THREADING_ERROR_BYPASS = builder
                .comment("Legacy compatibility key retained for existing configs and diagnostics. The modern main-thread deferred bridge has no worker timeout fallback.")
                .define("packetThreadingErrorBypass", false);
        PACKET_THREADING_WAIT_TIMEOUT_MS = builder
                .comment("Legacy compatibility value retained for existing configs and commands. The modern bridge never waits on packet-worker futures.")
                .defineInRange("packetThreadingWaitTimeoutMs", 50, 1, 1000);
        PACKET_THREADING_MAX_PENDING = builder
                .comment("Maximum pending deferred packet operations before the ordered queue is drained early on the server thread; queued packets are not discarded.")
                .defineInRange("packetThreadingMaxPending", 4096, 1, 65536);
        PACKET_THREADING_FALLBACK_CLEAR_THRESHOLD = builder
                .comment("Legacy compatibility value retained for existing configs and commands. Normal modern dispatch is already server-thread owned.")
                .defineInRange("packetThreadingFallbackClearThreshold", 5, 1, 1000);
        builder.pop();
        builder.pop();
    }

    public static boolean packetThreadingEnabled() {
        return ENABLE_PACKET_THREADING == null || ENABLE_PACKET_THREADING.get();
    }

    public static boolean packetThreadingErrorBypass() {
        return PACKET_THREADING_ERROR_BYPASS != null && PACKET_THREADING_ERROR_BYPASS.get();
    }

    public static int packetThreadingWaitTimeoutMs(int fallback) {
        return configuredInt(PACKET_THREADING_WAIT_TIMEOUT_MS, fallback);
    }

    public static int packetThreadingMaxPending(int fallback) {
        return configuredInt(PACKET_THREADING_MAX_PENDING, fallback);
    }

    public static int packetThreadingFallbackClearThreshold(int fallback) {
        return configuredInt(PACKET_THREADING_FALLBACK_CLEAR_THRESHOLD, fallback);
    }

    private static int configuredInt(ForgeConfigSpec.IntValue value, int fallback) {
        return value == null ? fallback : value.get();
    }

    private NetworkConfig() {
    }
}
