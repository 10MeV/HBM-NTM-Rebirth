package com.hbm.ntm.neutron;

public final class RBMKControlRodPlanner {
    public static final double MANUAL_SURGE_THRESHOLD = 0.01D;

    private RBMKControlRodPlanner() {
    }

    public static ManualTargetPlan planManualTarget(RBMKControlState state, double targetLevel) {
        double startingLevel = state == null ? 0.0D : state.level();
        if (state != null) {
            state.setTargetLevel(targetLevel);
        }
        return new ManualTargetPlan(startingLevel, targetLevel);
    }

    public static double manualMultiplier(RBMKControlState state, double startingLevel, double surgeModifier) {
        if (state == null) {
            return 0.0D;
        }
        double surge = 0.0D;
        if (state.targetLevel() < startingLevel
                && Math.abs(state.level() - state.targetLevel()) > MANUAL_SURGE_THRESHOLD) {
            surge = Math.sin(Math.pow(1.0D - state.level(), 15.0D) * Math.PI)
                    * (startingLevel - state.targetLevel())
                    * Math.max(0.0D, surgeModifier);
        }
        return state.level() + surge;
    }

    public static ColorTogglePlan planColorToggle(RBMKColor currentColor, int rawColor) {
        int index = Math.abs(rawColor) % RBMKColor.values().length;
        RBMKColor selected = RBMKColor.values()[index];
        return selected == currentColor
                ? new ColorTogglePlan(null, true)
                : new ColorTogglePlan(selected, false);
    }

    public static ManualTargetPlan planSetRods(RBMKControlState state, int percent) {
        int clamped = clamp(percent, 0, 100);
        return planManualTarget(state, clamped / 100.0D);
    }

    public static ManualTargetPlan planExtendRods(RBMKControlState state, int percentDelta) {
        int clamped = clamp(percentDelta, -100, 100);
        double currentTarget = state == null ? 0.0D : state.targetLevel();
        return planManualTarget(state, clamp01(currentTarget + clamped / 100.0D));
    }

    public static AutoTargetPlan planAutoTarget(double heat, AutoSettings settings) {
        AutoSettings safeSettings = settings == null ? AutoSettings.DEFAULT : settings;
        double lowerBound = Math.min(safeSettings.heatLower(), safeSettings.heatUpper());
        double upperBound = Math.max(safeSettings.heatLower(), safeSettings.heatUpper());
        double fauxLevel;

        if (heat < lowerBound) {
            fauxLevel = safeSettings.levelLower();
        } else if (heat > upperBound) {
            fauxLevel = safeSettings.levelUpper();
        } else {
            fauxLevel = switch (safeSettings.function()) {
                case LINEAR -> (heat - safeSettings.heatLower())
                        * ((safeSettings.levelUpper() - safeSettings.levelLower())
                        / (safeSettings.heatUpper() - safeSettings.heatLower()))
                        + safeSettings.levelLower();
                case QUAD_UP -> Math.pow((heat - safeSettings.heatLower())
                        / (safeSettings.heatUpper() - safeSettings.heatLower()), 2.0D)
                        * (safeSettings.levelUpper() - safeSettings.levelLower())
                        + safeSettings.levelLower();
                case QUAD_DOWN -> Math.pow((heat - safeSettings.heatUpper())
                        / (safeSettings.heatLower() - safeSettings.heatUpper()), 2.0D)
                        * (safeSettings.levelLower() - safeSettings.levelUpper())
                        + safeSettings.levelUpper();
            };
        }

        double target = clamp01(fauxLevel * 0.01D);
        return new AutoTargetPlan(fauxLevel, target);
    }

    public static AutoFunctionPlan planAutoFunctionPacket(int rawFunction) {
        int legacyModulo = Math.abs(rawFunction) % RBMKColor.values().length;
        if (legacyModulo >= RBMKFunction.values().length) {
            return new AutoFunctionPlan(false, null, legacyModulo);
        }
        return new AutoFunctionPlan(true, RBMKFunction.values()[legacyModulo], legacyModulo);
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

    private static double clamp01(double value) {
        if (value < 0.0D || Double.isNaN(value)) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }

    public enum RBMKColor {
        RED,
        YELLOW,
        GREEN,
        BLUE,
        PURPLE
    }

    public enum RBMKFunction {
        LINEAR,
        QUAD_UP,
        QUAD_DOWN
    }

    public record ManualTargetPlan(double startingLevel, double targetLevel) {
    }

    public record ColorTogglePlan(RBMKColor color, boolean cleared) {
    }

    public record AutoSettings(
            double levelLower,
            double levelUpper,
            double heatLower,
            double heatUpper,
            RBMKFunction function) {
        public static final AutoSettings DEFAULT =
                new AutoSettings(0.0D, 0.0D, 0.0D, 0.0D, RBMKFunction.LINEAR);

        public AutoSettings {
            function = function == null ? RBMKFunction.LINEAR : function;
        }
    }

    public record AutoTargetPlan(double fauxLevelPercent, double targetLevel) {
    }

    public record AutoFunctionPlan(boolean accepted, RBMKFunction function, int legacyModuloIndex) {
    }
}
