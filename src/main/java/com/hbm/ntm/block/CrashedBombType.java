package com.hbm.ntm.block;

import net.minecraft.util.StringRepresentable;

public enum CrashedBombType implements StringRepresentable {
    BALEFIRE, CONVENTIONAL, NUKE, SALTED;

    public static CrashedBombType byLegacyOrdinal(int ordinal) {
        CrashedBombType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : BALEFIRE;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
