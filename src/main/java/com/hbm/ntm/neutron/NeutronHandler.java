package com.hbm.ntm.neutron;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class NeutronHandler {
    private static final int CACHE_TIME_TICKS = 20;
    private static int ticks;

    private NeutronHandler() {
    }

    public static void tick(MinecraftServer server) {
        boolean cacheClear = ticks >= CACHE_TIME_TICKS;
        if (cacheClear) {
            ticks = 0;
        }
        ticks++;

        NeutronNodeWorld.removeEmptyWorlds();
        for (ServerLevel level : server.getAllLevels()) {
            NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getWorld(level);
            if (streamWorld == null) {
                continue;
            }

            streamWorld.runStreamInteractions(level);
            streamWorld.removeAllStreams();
            if (cacheClear) {
                streamWorld.cleanNodes();
            }
        }
    }
}
