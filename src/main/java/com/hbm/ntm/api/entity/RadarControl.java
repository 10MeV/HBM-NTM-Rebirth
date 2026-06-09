package com.hbm.ntm.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Arrays;
import java.util.Optional;

public enum RadarControl {
    SCAN_MISSILES(0, "missiles"),
    SCAN_SHELLS(1, "shells"),
    SCAN_PLAYERS(2, "players"),
    SMART_MODE(3, "smart"),
    REDSTONE_MODE(4, "red"),
    SHOW_MAP(5, "map"),
    CLEAR_MAP(6, "clear");

    public static final String TAG_CONTROL = "control";

    private final int id;
    private final String legacyKey;

    RadarControl(int id, String legacyKey) {
        this.id = id;
        this.legacyKey = legacyKey;
    }

    public int id() {
        return id;
    }

    public String legacyKey() {
        return legacyKey;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_CONTROL, id);
        return tag;
    }

    public CompoundTag toLegacyTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(legacyKey, true);
        return tag;
    }

    public static Optional<RadarControl> byId(int id) {
        return Arrays.stream(values()).filter(control -> control.id == id).findFirst();
    }

    public static Optional<RadarControl> fromTag(CompoundTag tag) {
        if (tag == null) {
            return Optional.empty();
        }
        if (tag.contains(TAG_CONTROL, Tag.TAG_INT)) {
            return byId(tag.getInt(TAG_CONTROL));
        }
        return Arrays.stream(values()).filter(control -> tag.contains(control.legacyKey)).findFirst();
    }

    public static boolean isControlTag(CompoundTag tag) {
        return fromTag(tag).isPresent();
    }
}
