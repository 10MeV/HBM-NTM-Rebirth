package com.hbm.ntm.util;

import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public final class HbmGameRuleUtil {
    private HbmGameRuleUtil() {
    }

    public static double getClampedDouble(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback, double min, double max) {
        return Mth.clamp(parseDouble(level, rule, fallback), min, max);
    }

    public static int getClampedInt(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback, int min, int max) {
        return Mth.clamp(parseInt(level, rule, fallback), min, max);
    }

    public static double getDoubleMinimum(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback, double min) {
        return Math.max(parseDouble(level, rule, fallback), min);
    }

    public static int getIntegerMinimum(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback, int min) {
        return Math.max(parseInt(level, rule, fallback), min);
    }

    public static double parseDouble(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback) {
        if (level == null || rule == null) {
            return fallback;
        }
        return parseDouble(level.getGameRules().getRule(rule).toString(), fallback);
    }

    public static int parseInt(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback) {
        if (level == null || rule == null) {
            return fallback;
        }
        return parseInt(level.getGameRules().getRule(rule).toString(), fallback);
    }

    public static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
