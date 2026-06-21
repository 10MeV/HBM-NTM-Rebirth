package com.hbm.ntm.neutron;

import java.util.EnumMap;
import java.util.Map;

public final class RBMKDialPlanner {
    private RBMKDialPlanner() {
    }

    public static EnumMap<DialKey, String> legacyDefaultRules() {
        EnumMap<DialKey, String> rules = new EnumMap<>(DialKey.class);
        for (DialKey key : DialKey.values()) {
            rules.put(key, key.defaultValue());
        }
        return rules;
    }

    public static DialCreatePlan planCreate(Map<DialKey, String> existingRules, boolean forceRecreate) {
        boolean saveDials = readBoolean(existingRules, DialKey.SAVE_DIALS);
        boolean writeDefaults = forceRecreate || !saveDials;
        return new DialCreatePlan(writeDefaults, writeDefaults ? legacyDefaultRules() : new EnumMap<>(DialKey.class));
    }

    public static RBMKDialSettings refresh(Map<DialKey, String> rules) {
        return new RBMKDialSettings(
                readDoubleMin(rules, DialKey.PASSIVE_COOLING, 0.0D),
                readDoubleMin(rules, DialKey.PASSIVE_COOLING_INNER, 0.0D),
                readDoubleClamped(rules, DialKey.COLUMN_HEAT_FLOW, 0.0D, 1.0D),
                readDoubleMin(rules, DialKey.FUEL_DIFFUSION_MOD, 0.0D),
                readDoubleClamped(rules, DialKey.HEAT_PROVISION, 0.0D, 1.0D),
                readLegacyColumnHeightAbove(rules),
                readBoolean(rules, DialKey.PERMANENT_SCRAP),
                readDoubleMin(rules, DialKey.BOILER_HEAT_CONSUMPTION, 0.0D),
                readDoubleMin(rules, DialKey.CONTROL_SPEED_MOD, 0.0D),
                readDoubleMin(rules, DialKey.REACTIVITY_MOD, 0.0D),
                readDoubleMin(rules, DialKey.OUTGASSER_MOD, 0.0D),
                readDoubleMin(rules, DialKey.SURGE_MOD, 0.0D),
                readIntClamped(rules, DialKey.FLUX_RANGE, 1, 100),
                readIntClamped(rules, DialKey.REASIM_RANGE, 1, 100),
                readBoolean(rules, DialKey.REASIM_BOILERS),
                readDoubleClamped(rules, DialKey.REASIM_BOILER_SPEED, 0.0D, 1.0D),
                readBoolean(rules, DialKey.DISABLE_MELTDOWNS),
                readBoolean(rules, DialKey.ENABLE_MELTDOWN_OVERPRESSURE),
                readDoubleClamped(rules, DialKey.MODERATOR_EFFICIENCY, 0.0D, 1.0D),
                readDoubleClamped(rules, DialKey.ABSORBER_EFFICIENCY, 0.0D, 1.0D),
                readDoubleClamped(rules, DialKey.REFLECTOR_EFFICIENCY, 0.0D, 1.0D),
                !readBoolean(rules, DialKey.DISABLE_DEPLETION),
                !readBoolean(rules, DialKey.DISABLE_XENON),
                readDoubleClamped(rules, DialKey.ABSORBER_HEAT_CONVERSION, 0.0D, 1.0D));
    }

    public static Object refreshValue(DialKey key, String rawValue) {
        if (key == null) {
            return "";
        }
        return switch (key) {
            case SAVE_DIALS, PERMANENT_SCRAP, REASIM_BOILERS, DISABLE_MELTDOWNS,
                    ENABLE_MELTDOWN_OVERPRESSURE, DISABLE_DEPLETION, DISABLE_XENON ->
                    Boolean.parseBoolean(valueOrDefault(rawValue, key));
            case COLUMN_HEIGHT -> readLegacyColumnHeightAbove(Map.of(key, valueOrDefault(rawValue, key)));
            case FLUX_RANGE, REASIM_RANGE -> readIntClamped(Map.of(key, valueOrDefault(rawValue, key)), key, 1, 100);
            case COLUMN_HEAT_FLOW, HEAT_PROVISION, REASIM_BOILER_SPEED, MODERATOR_EFFICIENCY,
                    ABSORBER_EFFICIENCY, REFLECTOR_EFFICIENCY, ABSORBER_HEAT_CONVERSION ->
                    readDoubleClamped(Map.of(key, valueOrDefault(rawValue, key)), key, 0.0D, 1.0D);
            case PASSIVE_COOLING, PASSIVE_COOLING_INNER, FUEL_DIFFUSION_MOD, BOILER_HEAT_CONSUMPTION,
                    CONTROL_SPEED_MOD, REACTIVITY_MOD, OUTGASSER_MOD, SURGE_MOD ->
                    readDoubleMin(Map.of(key, valueOrDefault(rawValue, key)), key, 0.0D);
        };
    }

    public static double clientSideLegacySurgeFallback(Map<DialKey, String> rules) {
        return readDoubleMin(rules, DialKey.PASSIVE_COOLING, 0.0D);
    }

    private static boolean readBoolean(Map<DialKey, String> rules, DialKey key) {
        return Boolean.parseBoolean(valueOrDefault(rules == null ? null : rules.get(key), key));
    }

    private static double readDoubleMin(Map<DialKey, String> rules, DialKey key, double min) {
        return Math.max(parseDouble(valueOrDefault(rules == null ? null : rules.get(key), key), key.doubleDefault()), min);
    }

    private static double readDoubleClamped(Map<DialKey, String> rules, DialKey key, double min, double max) {
        return clamp(parseDouble(valueOrDefault(rules == null ? null : rules.get(key), key), key.doubleDefault()),
                min, max);
    }

    private static int readIntClamped(Map<DialKey, String> rules, DialKey key, int min, int max) {
        return clamp(parseInt(valueOrDefault(rules == null ? null : rules.get(key), key), key.intDefault()),
                min, max);
    }

    private static int readLegacyColumnHeightAbove(Map<DialKey, String> rules) {
        return readIntClamped(rules, DialKey.COLUMN_HEIGHT, 2, 16) - 1;
    }

    private static String valueOrDefault(String value, DialKey key) {
        return value == null || value.isEmpty() ? key.defaultValue() : value;
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static double clamp(double value, double min, double max) {
        if (value < min || Double.isNaN(value)) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public enum DialKey {
        SAVE_DIALS("dialSaveDials", "true"),
        PASSIVE_COOLING("dialPassiveCooling", "2.5"),
        PASSIVE_COOLING_INNER("dialPassiveCoolingInner", "0.1"),
        COLUMN_HEAT_FLOW("dialColumnHeatFlow", "0.2"),
        FUEL_DIFFUSION_MOD("dialDiffusionMod", "1.0"),
        HEAT_PROVISION("dialHeatProvision", "0.2"),
        COLUMN_HEIGHT("dialColumnHeight", "4"),
        PERMANENT_SCRAP("dialEnablePermaScrap", "true"),
        BOILER_HEAT_CONSUMPTION("dialBoilerHeatConsumption", "0.1"),
        CONTROL_SPEED_MOD("dialControlSpeed", "1.0"),
        REACTIVITY_MOD("dialReactivityMod", "1.0"),
        OUTGASSER_MOD("dialOutgasserSpeedMod", "1.0"),
        SURGE_MOD("dialControlSurgeMod", "1.0"),
        FLUX_RANGE("dialFluxRange", "5"),
        REASIM_RANGE("dialReasimRange", "10"),
        REASIM_BOILERS("dialReasimBoilers", "false"),
        REASIM_BOILER_SPEED("dialReasimBoilerSpeed", "0.05"),
        DISABLE_MELTDOWNS("dialDisableMeltdowns", "false"),
        ENABLE_MELTDOWN_OVERPRESSURE("dialEnableMeltdownOverpressure", "false"),
        MODERATOR_EFFICIENCY("dialModeratorEfficiency", "1.0"),
        ABSORBER_EFFICIENCY("dialAbsorberEfficiency", "1.0"),
        REFLECTOR_EFFICIENCY("dialReflectorEfficiency", "1.0"),
        DISABLE_DEPLETION("dialDisableDepletion", "false"),
        DISABLE_XENON("dialDisableXenon", "false"),
        ABSORBER_HEAT_CONVERSION("dialAbsorberHeatConversion", "0.05");

        private final String legacyName;
        private final String defaultValue;

        DialKey(String legacyName, String defaultValue) {
            this.legacyName = legacyName;
            this.defaultValue = defaultValue;
        }

        public String legacyName() {
            return legacyName;
        }

        public String defaultValue() {
            return defaultValue;
        }

        double doubleDefault() {
            return Double.parseDouble(defaultValue);
        }

        int intDefault() {
            return Integer.parseInt(defaultValue);
        }
    }

    public record DialCreatePlan(boolean writeDefaults, EnumMap<DialKey, String> defaultsToWrite) {
    }
}
