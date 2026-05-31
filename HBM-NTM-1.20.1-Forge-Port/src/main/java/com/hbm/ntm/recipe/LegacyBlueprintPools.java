package com.hbm.ntm.recipe;

public final class LegacyBlueprintPools {
    public static final String PREFIX_ALT = "alt.";
    public static final String PREFIX_DISCOVER = "discover.";
    public static final String PREFIX_SECRET = "secret.";
    public static final String PREFIX_528 = "528.";

    private LegacyBlueprintPools() {
    }

    public static Kind kind(String pool) {
        if (pool == null) {
            return Kind.NORMAL;
        }
        if (pool.startsWith(PREFIX_ALT)) {
            return Kind.ALTERNATE;
        }
        if (pool.startsWith(PREFIX_DISCOVER)) {
            return Kind.DISCOVERABLE;
        }
        if (pool.startsWith(PREFIX_SECRET)) {
            return Kind.SECRET;
        }
        if (pool.startsWith(PREFIX_528)) {
            return Kind.MODE_528;
        }
        return Kind.NORMAL;
    }

    public static boolean is528(String pool) {
        return kind(pool) == Kind.MODE_528;
    }

    public enum Kind {
        NORMAL,
        ALTERNATE,
        DISCOVERABLE,
        SECRET,
        MODE_528
    }
}
