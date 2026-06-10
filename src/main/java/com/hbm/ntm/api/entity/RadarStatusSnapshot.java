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
        scanSettings.writeTo(tag);
        redstoneMode().writeTo(tag);
        tag.putBoolean(TAG_SHOW_MAP, showMap);
    }

    public static RadarStatusSnapshot fromTag(CompoundTag tag) {
        RadarDetectable.RadarScanParams scanSettings = RadarDetectable.RadarScanParams.fromTag(tag);
        RadarRedstoneMode redstoneMode = RadarRedstoneMode.fromTag(tag);
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
                redstoneMode.legacyFlag(),
                tag.getBoolean(TAG_SHOW_MAP));
    }

    public RadarRedstoneMode redstoneMode() {
        return RadarRedstoneMode.fromLegacyFlag(redstoneProximityMode);
    }

    private static int intOrLegacy(CompoundTag tag, String key, String legacyKey) {
        return tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : tag.getInt(legacyKey);
    }
}
