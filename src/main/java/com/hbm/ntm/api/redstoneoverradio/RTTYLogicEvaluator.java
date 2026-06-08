package com.hbm.ntm.api.redstoneoverradio;

public final class RTTYLogicEvaluator {
    public static int evaluate(String signal, String[] mapping, int[] conditions, boolean descending) {
        String safeSignal = signal == null ? "" : signal;
        if (descending) {
            for (int i = 15; i >= 0; i--) {
                if (!RTTYSignalMapper.mappingValue(mapping, i).isEmpty()
                        && parseSignal(safeSignal, RTTYSignalMapper.mappingValue(mapping, i), condition(conditions, i))) {
                    return i;
                }
            }
            return 0;
        }
        for (int i = 0; i <= 15; i++) {
            if (!RTTYSignalMapper.mappingValue(mapping, i).isEmpty()
                    && parseSignal(safeSignal, RTTYSignalMapper.mappingValue(mapping, i), condition(conditions, i))) {
                return i;
            }
        }
        return 0;
    }

    public static boolean parseSignal(String signal, String mappedValue, int condition) {
        if (condition <= 5) {
            long sig;
            long map;
            try {
                sig = Long.parseLong(signal);
                map = Long.parseLong(mappedValue);
            } catch (NumberFormatException ignored) {
                return false;
            }

            return switch (condition) {
                case 1 -> sig <= map;
                case 2 -> sig >= map;
                case 3 -> sig > map;
                case 4 -> sig == map;
                case 5 -> sig != map;
                default -> sig < map;
            };
        }

        return switch (condition) {
            case 7 -> !signal.equals(mappedValue);
            case 8 -> signal.contains(mappedValue);
            case 9 -> !signal.contains(mappedValue);
            default -> signal.equals(mappedValue);
        };
    }

    private static int condition(int[] conditions, int index) {
        if (conditions == null || index < 0 || index >= conditions.length) {
            return 0;
        }
        return conditions[index];
    }

    private RTTYLogicEvaluator() {
    }
}
