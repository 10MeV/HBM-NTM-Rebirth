package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import java.util.List;

public enum RadarRedstoneMode {
    PROXIMITY(true),
    TIER(false);

    public static final String TAG_RED_MODE = "redMode";

    private final boolean legacyFlag;

    RadarRedstoneMode(boolean legacyFlag) {
        this.legacyFlag = legacyFlag;
    }

    public boolean legacyFlag() {
        return legacyFlag;
    }

    public int power(List<RadarEntry> entries, BlockPos origin, int range) {
        return this == PROXIMITY ? proximityPower(entries, origin, range) : tierPower(entries);
    }

    public void writeTo(CompoundTag tag) {
        tag.putBoolean(TAG_RED_MODE, legacyFlag);
    }

    public void writeLegacyWire(FriendlyByteBuf buffer) {
        buffer.writeBoolean(legacyFlag);
    }

    public static RadarRedstoneMode fromLegacyFlag(boolean redMode) {
        return redMode ? PROXIMITY : TIER;
    }

    public static RadarRedstoneMode fromTag(CompoundTag tag) {
        return fromTag(tag, PROXIMITY);
    }

    public static RadarRedstoneMode fromTag(CompoundTag tag, RadarRedstoneMode fallback) {
        RadarRedstoneMode defaultMode = fallback != null ? fallback : PROXIMITY;
        return tag != null && tag.contains(TAG_RED_MODE, Tag.TAG_BYTE)
                ? fromLegacyFlag(tag.getBoolean(TAG_RED_MODE))
                : defaultMode;
    }

    public static RadarRedstoneMode readLegacyWire(FriendlyByteBuf buffer) {
        return fromLegacyFlag(buffer.readBoolean());
    }

    private static int proximityPower(List<RadarEntry> entries, BlockPos origin, int range) {
        if (entries == null || entries.isEmpty() || origin == null || range <= 0) {
            return 0;
        }

        double maxRange = range * Math.sqrt(2.0D);
        int power = 0;
        for (RadarEntry entry : entries) {
            if (!entry.redstone()) {
                continue;
            }
            double dist = Math.sqrt(Math.pow(entry.pos().getX() - origin.getX(), 2.0D)
                    + Math.pow(entry.pos().getZ() - origin.getZ(), 2.0D));
            int candidate = 15 - (int) Math.floor(dist / maxRange * 15.0D);
            power = Math.max(power, Mth.clamp(candidate, 0, 15));
        }
        return power;
    }

    private static int tierPower(List<RadarEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }

        int power = 0;
        for (RadarEntry entry : entries) {
            if (entry.redstone()) {
                power = Math.max(power, entry.blipLevel() + 1);
            }
        }
        return Mth.clamp(power, 0, 15);
    }
}
