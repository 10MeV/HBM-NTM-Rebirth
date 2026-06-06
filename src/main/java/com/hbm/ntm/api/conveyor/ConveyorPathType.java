package com.hbm.ntm.api.conveyor;

import net.minecraft.util.StringRepresentable;

public enum ConveyorPathType implements StringRepresentable {
    STRAIGHT(0, "straight"),
    LEFT(1, "left"),
    RIGHT(2, "right");

    private final int legacyOffset;
    private final String serializedName;

    ConveyorPathType(int legacyOffset, String serializedName) {
        this.legacyOffset = legacyOffset;
        this.serializedName = serializedName;
    }

    public int legacyOffset() {
        return legacyOffset;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public static ConveyorPathType fromLegacyMetadata(int metadata) {
        if (metadata >= 6 && metadata <= 9) {
            return LEFT;
        }
        if (metadata >= 10 && metadata <= 13) {
            return RIGHT;
        }
        return STRAIGHT;
    }
}
