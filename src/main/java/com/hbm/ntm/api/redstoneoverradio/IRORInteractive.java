package com.hbm.ntm.api.redstoneoverradio;

/**
 * Legacy-name bridge for Redstone-over-Radio command endpoints.
 */
@Deprecated(forRemoval = false)
public interface IRORInteractive extends IRORInfo, RORInteractive {
    static String getCommand(String input) {
        return RORInteractive.getCommand(input);
    }

    static String[] getParams(String input) {
        return RORInteractive.getParams(input);
    }

    static int parseInt(String value, int min, int max) {
        return RORInteractive.parseInt(value, min, max);
    }
}
