package com.hbm.ntm.neutron;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Function;

public final class NeutronHandler {
    private static final int CACHE_TIME_TICKS = 20;
    private static final Function<ServerLevel, RBMKNeutronSettings> LEGACY_DEFAULT_RBMK_SETTINGS =
            level -> RBMKDialSettings.legacyDefaults().toNeutronSettings();
    private static int ticks;
    private static Function<ServerLevel, RBMKNeutronSettings> rbmkNeutronSettingsProvider = LEGACY_DEFAULT_RBMK_SETTINGS;

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

            RBMKNeutronHandler.setSettings(rbmkNeutronSettingsProvider.apply(level));
            streamWorld.runStreamInteractions(level);
            streamWorld.removeAllStreams();
            if (cacheClear) {
                streamWorld.cleanNodes();
            }
        }
    }

    public static void setRBMKNeutronSettingsProvider(Function<ServerLevel, RBMKNeutronSettings> provider) {
        rbmkNeutronSettingsProvider = provider == null ? LEGACY_DEFAULT_RBMK_SETTINGS : provider;
    }

    public static void resetRBMKNeutronSettingsProvider() {
        rbmkNeutronSettingsProvider = LEGACY_DEFAULT_RBMK_SETTINGS;
    }
}
