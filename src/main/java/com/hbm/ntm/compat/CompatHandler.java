package com.hbm.ntm.compat;

import com.hbm.ntm.fluid.FluidType;
import java.util.List;

/**
 * Modern landing point for the non-computer pieces of the legacy CompatHandler.
 */
public final class CompatHandler {
    public static Object[] steamTypeToInt(FluidType type) {
        return new Object[] { Compat.steamTypeToInt(type) };
    }

    public static FluidType intToSteamType(int compressionLevel) {
        return Compat.intToSteamType(compressionLevel);
    }

    public static boolean computerIntegrationPaused() {
        return true;
    }

    public static String computerIntegrationStatus() {
        return "paused until explicitly requested";
    }

    public static List<String> optionalCompatModIds() {
        return List.of(
                Compat.MOD_JEI,
                Compat.MOD_AE2,
                Compat.MOD_ENERGY_CONTROL,
                Compat.MOD_GTCEU,
                Compat.MOD_GREGTECH,
                Compat.MOD_REACTORCRAFT,
                Compat.MOD_ET_FUTURUM,
                Compat.MOD_TCONSTRUCT,
                Compat.MOD_GALACTICRAFT,
                Compat.MOD_ADVANCED_ROCKETRY,
                Compat.MOD_RAILCRAFT,
                Compat.MOD_TORCHERINO);
    }

    public static List<String> loadedOptionalCompatModIds() {
        return Compat.loadedModIds(optionalCompatModIds());
    }

    public static String optionalCompatSummary() {
        return Compat.optionalModSummary(optionalCompatModIds());
    }

    private CompatHandler() {
    }
}
