package com.hbm.ntm.util;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/**
 * Legacy-name game rule parsing facade.
 */
@Deprecated(forRemoval = false)
public final class GameRuleHelper {
    private GameRuleHelper() {
    }

    public static double getClampedDouble(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback, double min, double max) {
        return HbmGameRuleUtil.getClampedDouble(level, rule, fallback, min, max);
    }

    public static int getClampedInt(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback, int min, int max) {
        return HbmGameRuleUtil.getClampedInt(level, rule, fallback, min, max);
    }

    public static double getDoubleMinimum(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback, double min) {
        return HbmGameRuleUtil.getDoubleMinimum(level, rule, fallback, min);
    }

    public static int getIntegerMinimum(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback, int min) {
        return HbmGameRuleUtil.getIntegerMinimum(level, rule, fallback, min);
    }

    public static double parseDouble(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, double fallback) {
        return HbmGameRuleUtil.parseDouble(level, rule, fallback);
    }

    public static int parseInt(Level level, GameRules.Key<? extends GameRules.Value<?>> rule, int fallback) {
        return HbmGameRuleUtil.parseInt(level, rule, fallback);
    }

    public static double parseDouble(String value, double fallback) {
        return HbmGameRuleUtil.parseDouble(value, fallback);
    }

    public static int parseInt(String value, int fallback) {
        return HbmGameRuleUtil.parseInt(value, fallback);
    }
}
