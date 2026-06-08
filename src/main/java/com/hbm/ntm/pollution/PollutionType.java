package com.hbm.ntm.pollution;

import java.util.Locale;

public enum PollutionType {
    SOOT,
    POISON,
    HEAVYMETAL,
    FALLOUT;

    public static PollutionType byName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.toUpperCase(Locale.ROOT).replace("_", "");
        for (PollutionType type : values()) {
            if (type.name().replace("_", "").equals(normalized)) {
                return type;
            }
        }
        return null;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
