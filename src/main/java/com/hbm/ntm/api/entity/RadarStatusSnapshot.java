package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public record RadarStatusSnapshot(BlockPos pos, int range, long power, long maxPower, boolean jammed,
                                  int entryAmount, int redstonePower,
                                  RadarDetectable.RadarScanParams scanSettings,
                                  boolean redstoneProximityMode, boolean showMap) {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_LEGACY_X = "posX";
    private static final String TAG_LEGACY_Y = "posY";
    private static final String TAG_LEGACY_Z = "posZ";
    private static final String TAG_RANGE = "range";
    private static final String TAG_POWER = "power";
    private static final String TAG_MAX_POWER = "maxPower";
    private static final String TAG_JAMMED = "jammed";
    private static final String TAG_ENTRY_AMOUNT = "amount";
    private static final String TAG_REDSTONE_POWER = "redstone";
    private static final String TAG_SCAN_MISSILES = "scanMissiles";
    private static final String TAG_SCAN_SHELLS = "scanShells";
    private static final String TAG_SCAN_PLAYERS = "scanPlayers";
    private static final String TAG_SMART_MODE = "smartMode";
    private static final String TAG_REDSTONE_PROXIMITY_MODE = "redMode";
    private static final String TAG_SHOW_MAP = "showMap";

    public RadarStatusSnapshot {
        pos = pos == null ? BlockPos.ZERO : pos.immutable();
        range = Math.max(0, range);
        power = Math.max(0L, power);
        maxPower = Math.max(0L, maxPower);
        entryAmount = Math.max(0, entryAmount);
        redstonePower = Math.max(0, Math.min(15, redstonePower));
        scanSettings = scanSettings == null ? RadarDetectable.RadarScanParams.DEFAULT : scanSettings;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        writeTo(tag);
        return tag;
    }

    public void writeTo(CompoundTag tag) {
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putInt(TAG_RANGE, range);
        tag.putLong(TAG_POWER, power);
        tag.putLong(TAG_MAX_POWER, maxPower);
        tag.putBoolean(TAG_JAMMED, jammed);
        tag.putInt(TAG_ENTRY_AMOUNT, entryAmount);
        tag.putInt(TAG_REDSTONE_POWER, redstonePower);
        tag.putBoolean(TAG_SCAN_MISSILES, scanSettings.scanMissiles());
        tag.putBoolean(TAG_SCAN_SHELLS, scanSettings.scanShells());
        tag.putBoolean(TAG_SCAN_PLAYERS, scanSettings.scanPlayers());
        tag.putBoolean(TAG_SMART_MODE, scanSettings.smartMode());
        tag.putBoolean(TAG_REDSTONE_PROXIMITY_MODE, redstoneProximityMode);
        tag.putBoolean(TAG_SHOW_MAP, showMap);
    }

    public static RadarStatusSnapshot fromTag(CompoundTag tag) {
        RadarDetectable.RadarScanParams scanSettings = new RadarDetectable.RadarScanParams(
                tag.getBoolean(TAG_SCAN_MISSILES),
                tag.getBoolean(TAG_SCAN_SHELLS),
                tag.getBoolean(TAG_SCAN_PLAYERS),
                tag.getBoolean(TAG_SMART_MODE));
        return new RadarStatusSnapshot(
                new BlockPos(intOrLegacy(tag, TAG_X, TAG_LEGACY_X), intOrLegacy(tag, TAG_Y, TAG_LEGACY_Y),
                        intOrLegacy(tag, TAG_Z, TAG_LEGACY_Z)),
                tag.getInt(TAG_RANGE),
                tag.getLong(TAG_POWER),
                tag.getLong(TAG_MAX_POWER),
                tag.getBoolean(TAG_JAMMED),
                tag.getInt(TAG_ENTRY_AMOUNT),
                tag.getInt(TAG_REDSTONE_POWER),
                scanSettings,
                tag.getBoolean(TAG_REDSTONE_PROXIMITY_MODE),
                tag.getBoolean(TAG_SHOW_MAP));
    }

    private static int intOrLegacy(CompoundTag tag, String key, String legacyKey) {
        return tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : tag.getInt(legacyKey);
    }
}
