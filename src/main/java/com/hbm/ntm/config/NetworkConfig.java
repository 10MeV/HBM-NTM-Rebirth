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
                .comment("Legacy 0.01_enablePacketThreading: use a separate packet send worker for selected high-frequency server-to-client packets.")
                .define("enablePacketThreading", true);
        PACKET_THREADING_ERROR_BYPASS = builder
                .comment("Legacy 0.04_packetThreadingErrorBypass: ignore packet threading timeout fallback and keep using the worker. Only enable for debugging.")
                .define("packetThreadingErrorBypass", false);
        PACKET_THREADING_WAIT_TIMEOUT_MS = builder
                .comment("Legacy PacketThreading wait budget in milliseconds before queued operations are discarded for this tick.")
                .defineInRange("packetThreadingWaitTimeoutMs", 50, 1, 1000);
        PACKET_THREADING_MAX_PENDING = builder
                .comment("Maximum pending threaded packet operations before the dispatcher clears queued work and falls back to immediate sending for the new packet.")
                .defineInRange("packetThreadingMaxPending", 4096, 1, 65536);
        PACKET_THREADING_FALLBACK_CLEAR_THRESHOLD = builder
                .comment("Legacy clear-count threshold before packet threading switches to main-thread fallback. Set higher if long send queues are expected.")
                .defineInRange("packetThreadingFallbackClearThreshold", 5, 1, 1000);
        builder.pop();
        builder.pop();
    }

    private NetworkConfig() {
    }
}
