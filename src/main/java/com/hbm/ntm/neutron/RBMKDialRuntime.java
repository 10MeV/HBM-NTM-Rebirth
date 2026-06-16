package com.hbm.ntm.neutron;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;

import java.util.EnumMap;
import java.util.Map;

public final class RBMKDialRuntime {
    private static final int DOUBLE_SCALE = 1_000_000;
    private static final EnumMap<RBMKDialPlanner.DialKey, GameRules.Key<?>> RULES =
            new EnumMap<>(RBMKDialPlanner.DialKey.class);
    private static boolean bootstrapped;

    private RBMKDialRuntime() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        for (RBMKDialPlanner.DialKey key : RBMKDialPlanner.DialKey.values()) {
            RULES.put(key, registerRule(key));
        }
        NeutronHandler.setRBMKNeutronSettingsProvider(level -> settings(level).toNeutronSettings());
        NeutronHandler.setRBMKRuntimeSettingsProvider(level -> settings(level).toRuntimeSettings());
        bootstrapped = true;
    }

    public static RBMKDialSettings settings(ServerLevel level) {
        return RBMKDialPlanner.refresh(readRules(level));
    }

    public static EnumMap<RBMKDialPlanner.DialKey, String> readRules(ServerLevel level) {
        ensureBootstrapped();
        EnumMap<RBMKDialPlanner.DialKey, String> values = new EnumMap<>(RBMKDialPlanner.DialKey.class);
        if (level == null) {
            values.putAll(RBMKDialPlanner.legacyDefaultRules());
            return values;
        }
        for (RBMKDialPlanner.DialKey key : RBMKDialPlanner.DialKey.values()) {
            values.put(key, readRule(level, key));
        }
        return values;
    }

    public static void createDials(ServerLevel level) {
        createDials(level, false);
    }

    public static void createDials(ServerLevel level, boolean forceRecreate) {
        if (level == null) {
            return;
        }
        ensureBootstrapped();
        Map<RBMKDialPlanner.DialKey, String> current = readRules(level);
        RBMKDialPlanner.DialCreatePlan plan = RBMKDialPlanner.planCreate(current, forceRecreate);
        if (!plan.writeDefaults()) {
            return;
        }
        MinecraftServer server = level.getServer();
        for (Map.Entry<RBMKDialPlanner.DialKey, String> entry : plan.defaultsToWrite().entrySet()) {
            writeRule(level, entry.getKey(), entry.getValue(), server);
        }
    }

    public static Object getGameRule(ServerLevel level, RBMKDialPlanner.DialKey key) {
        if (level == null || key == null) {
            return "";
        }
        return RBMKDialPlanner.refreshValue(key, readRule(level, key));
    }

    private static void ensureBootstrapped() {
        if (!bootstrapped) {
            bootstrap();
        }
    }

    private static GameRules.Key<?> registerRule(RBMKDialPlanner.DialKey key) {
        if (isBoolean(key)) {
            return GameRules.register(key.legacyName(), GameRules.Category.MISC,
                    GameRules.BooleanValue.create(Boolean.parseBoolean(key.defaultValue())));
        }
        int defaultValue = isScaledDouble(key)
                ? scaledDouble(key.defaultValue(), key.doubleDefault())
                : parseInt(key.defaultValue(), key.intDefault());
        return GameRules.register(key.legacyName(), GameRules.Category.MISC,
                GameRules.IntegerValue.create(defaultValue));
    }

    private static String readRule(ServerLevel level, RBMKDialPlanner.DialKey key) {
        GameRules.Key<?> rule = RULES.get(key);
        if (rule == null) {
            return key.defaultValue();
        }
        if (isBoolean(key)) {
            @SuppressWarnings("unchecked")
            GameRules.Key<GameRules.BooleanValue> booleanRule = (GameRules.Key<GameRules.BooleanValue>) rule;
            return Boolean.toString(level.getGameRules().getBoolean(booleanRule));
        }
        @SuppressWarnings("unchecked")
        GameRules.Key<GameRules.IntegerValue> integerRule = (GameRules.Key<GameRules.IntegerValue>) rule;
        int value = level.getGameRules().getInt(integerRule);
        if (isScaledDouble(key)) {
            return Double.toString(value / (double) DOUBLE_SCALE);
        }
        return Integer.toString(value);
    }

    private static void writeRule(ServerLevel level, RBMKDialPlanner.DialKey key, String value,
            MinecraftServer server) {
        GameRules.Key<?> rule = RULES.get(key);
        if (rule == null) {
            return;
        }
        if (isBoolean(key)) {
            @SuppressWarnings("unchecked")
            GameRules.Key<GameRules.BooleanValue> booleanRule = (GameRules.Key<GameRules.BooleanValue>) rule;
            level.getGameRules().getRule(booleanRule).set(Boolean.parseBoolean(value), server);
            return;
        }
        @SuppressWarnings("unchecked")
        GameRules.Key<GameRules.IntegerValue> integerRule = (GameRules.Key<GameRules.IntegerValue>) rule;
        int intValue = isScaledDouble(key)
                ? scaledDouble(value, key.doubleDefault())
                : parseInt(value, key.intDefault());
        level.getGameRules().getRule(integerRule).set(intValue, server);
    }

    private static boolean isBoolean(RBMKDialPlanner.DialKey key) {
        return switch (key) {
            case SAVE_DIALS, PERMANENT_SCRAP, REASIM_BOILERS, DISABLE_MELTDOWNS,
                    ENABLE_MELTDOWN_OVERPRESSURE, DISABLE_DEPLETION, DISABLE_XENON -> true;
            default -> false;
        };
    }

    private static boolean isScaledDouble(RBMKDialPlanner.DialKey key) {
        return switch (key) {
            case PASSIVE_COOLING, PASSIVE_COOLING_INNER, COLUMN_HEAT_FLOW, FUEL_DIFFUSION_MOD, HEAT_PROVISION,
                    BOILER_HEAT_CONSUMPTION, CONTROL_SPEED_MOD, REACTIVITY_MOD, OUTGASSER_MOD, SURGE_MOD,
                    REASIM_BOILER_SPEED, MODERATOR_EFFICIENCY, ABSORBER_EFFICIENCY, REFLECTOR_EFFICIENCY,
                    ABSORBER_HEAT_CONVERSION -> true;
            default -> false;
        };
    }

    private static int scaledDouble(String value, double fallback) {
        double parsed;
        try {
            parsed = Double.parseDouble(value);
        } catch (RuntimeException ignored) {
            parsed = fallback;
        }
        double scaled = parsed * DOUBLE_SCALE;
        if (scaled > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (scaled < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.round(scaled);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
