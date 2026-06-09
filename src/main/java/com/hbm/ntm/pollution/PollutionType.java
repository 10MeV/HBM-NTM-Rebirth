package com.hbm.ntm.pollution;

import java.util.List;
import java.util.Locale;

public enum PollutionType {
    SOOT,
    POISON,
    HEAVYMETAL,
    FALLOUT;

    private static final PollutionType[] ORDERED_VALUES = values();
    private static final List<PollutionType> ORDERED_LIST = List.of(ORDERED_VALUES);
    private static final List<PollutionType> LEGACY_DETECTOR_TYPES = List.of(SOOT, POISON, HEAVYMETAL);

    public static PollutionType byName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = normalize(name);
        for (PollutionType type : ORDERED_VALUES) {
            if (normalize(type.name()).equals(normalized) || normalize(type.id()).equals(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static PollutionType byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < ORDERED_VALUES.length ? ORDERED_VALUES[ordinal] : null;
    }

    public static int count() {
        return ORDERED_VALUES.length;
    }

    public static List<PollutionType> orderedValues() {
        return ORDERED_LIST;
    }

    public static List<PollutionType> legacyDetectorTypes() {
        return LEGACY_DETECTOR_TYPES;
    }

    public static String[] ids() {
        String[] ids = new String[ORDERED_VALUES.length];
        for (int i = 0; i < ORDERED_VALUES.length; i++) {
            ids[i] = ORDERED_VALUES[i].id();
        }
        return ids;
    }

    public String id() {
        return switch (this) {
            case SOOT -> "soot";
            case POISON -> "poison";
            case HEAVYMETAL -> "heavymetal";
            case FALLOUT -> "fallout";
        };
    }

    public String displayName() {
        return switch (this) {
            case SOOT -> "Soot";
            case POISON -> "Poison";
            case HEAVYMETAL -> "Heavy metal";
            case FALLOUT -> "Fallout";
        };
    }

    private static String normalize(String name) {
        return name.toUpperCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
    }
}
