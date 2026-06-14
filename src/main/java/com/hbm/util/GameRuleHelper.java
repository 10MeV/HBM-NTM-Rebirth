package com.hbm.util;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for game rule parsing helpers.
 */
@Deprecated(forRemoval = false)
public final class GameRuleHelper {
    private GameRuleHelper() {
    }

    public static double getClampedDouble(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback, double min, double max) {
        return com.hbm.ntm.util.GameRuleHelper.getClampedDouble(level, rule, fallback, min, max);
    }

    public static int getClampedInt(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback, int min, int max) {
        return com.hbm.ntm.util.GameRuleHelper.getClampedInt(level, rule, fallback, min, max);
    }

    public static double getDoubleMinimum(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback, double min) {
        return com.hbm.ntm.util.GameRuleHelper.getDoubleMinimum(level, rule, fallback, min);
    }

    public static int getIntegerMinimum(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback, int min) {
        return com.hbm.ntm.util.GameRuleHelper.getIntegerMinimum(level, rule, fallback, min);
    }

    public static double parseDouble(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback) {
        return com.hbm.ntm.util.GameRuleHelper.parseDouble(level, rule, fallback);
    }

    public static int parseInt(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback) {
        return com.hbm.ntm.util.GameRuleHelper.parseInt(level, rule, fallback);
    }

    public static double parseDouble(String value, double fallback) {
        return com.hbm.ntm.util.GameRuleHelper.parseDouble(value, fallback);
    }

    public static int parseInt(String value, int fallback) {
        return com.hbm.ntm.util.GameRuleHelper.parseInt(value, fallback);
    }
}
