package com.hbm.ntm.api.redstoneoverradio;

import net.minecraft.util.Mth;

public final class RTTYSignalMapper {
    public static final int REDSTONE_LEVELS = 16;

    public static String redstoneToSignal(int input, boolean customMap, String[] mapping) {
        int level = Mth.clamp(input, 0, 15);
        if (!customMap) {
            return Integer.toString(level);
        }
        return mappingValue(mapping, level);
    }

    public static int signalToRedstone(String signal, boolean customMap, String[] mapping) {
        if (signal == null) {
            return 0;
        }
        if (customMap) {
            for (int i = 15; i >= 0; i--) {
                if (signal.equals(mappingValue(mapping, i))) {
                    return i;
                }
            }
            return 0;
        }
        int parsed = 0;
        try {
            parsed = Integer.parseInt(signal);
        } catch (NumberFormatException ignored) {
        }
        return Mth.clamp(parsed, 0, 15);
    }

    public static boolean shouldReceive(RTTYSystem.RTTYChannel channel, long lastUpdate, boolean polling) {
        return channel != null && (polling || (channel.timeStamp() > lastUpdate - 1 && channel.timeStamp() != -1));
    }

    public static boolean isStalePollingSignal(RTTYSystem.RTTYChannel channel, long lastUpdate, boolean polling) {
        return channel != null && polling && channel.timeStamp() < lastUpdate - 2;
    }

    public static String mappingValue(String[] mapping, int index) {
        if (mapping == null || index < 0 || index >= mapping.length) {
            return "";
        }
        return mapping[index] == null ? "" : mapping[index];
    }

    public static String[] copyMapping(String[] mapping) {
        String[] result = new String[REDSTONE_LEVELS];
        for (int i = 0; i < result.length; i++) {
            result[i] = mappingValue(mapping, i);
        }
        return result;
    }

    private RTTYSignalMapper() {
    }
}
