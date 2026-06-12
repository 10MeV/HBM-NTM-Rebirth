package com.hbm.handler;

import com.hbm.ntm.fluid.FluidType;
import java.util.List;

/**
 * Legacy package facade for the non-computer pieces of CompatHandler.
 */
@Deprecated(forRemoval = false)
public final class CompatHandler {
    public static Object[] steamTypeToInt(FluidType type) {
        return com.hbm.ntm.compat.CompatHandler.steamTypeToInt(type);
    }

    public static FluidType intToSteamType(int compressionLevel) {
        return com.hbm.ntm.compat.CompatHandler.intToSteamType(compressionLevel);
    }

    public static boolean computerIntegrationPaused() {
        return com.hbm.ntm.compat.CompatHandler.computerIntegrationPaused();
    }

    public static String computerIntegrationStatus() {
        return com.hbm.ntm.compat.CompatHandler.computerIntegrationStatus();
    }

    public static List<String> optionalCompatModIds() {
        return com.hbm.ntm.compat.CompatHandler.optionalCompatModIds();
    }

    public static List<String> loadedOptionalCompatModIds() {
        return com.hbm.ntm.compat.CompatHandler.loadedOptionalCompatModIds();
    }

    public static String optionalCompatSummary() {
        return com.hbm.ntm.compat.CompatHandler.optionalCompatSummary();
    }

    private CompatHandler() {
    }
}
