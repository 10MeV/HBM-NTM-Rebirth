package com.hbm.ntm.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.List;

public record RadarSyncSnapshot(long power, RadarDetectable.RadarScanParams scanSettings,
                                 boolean redstoneProximityMode, boolean showMap, boolean jammed,
                                 int redstonePower, List<RadarEntry> entries, RadarMapUpdate mapUpdate) {
    private static final String TAG_POWER = "power";
    private static final String TAG_SHOW_MAP = "showMap";
    private static final String TAG_JAMMED = "jammed";
    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_LAST_RED_POWER = "lastPower";

    public RadarSyncSnapshot {
        power = Math.max(0L, power);
        scanSettings = scanSettings == null ? RadarDetectable.RadarScanParams.DEFAULT : scanSettings;
        redstonePower = Math.max(0, Math.min(15, redstonePower));
        entries = List.copyOf(entries != null ? entries : List.of());
        mapUpdate = mapUpdate == null ? RadarMapUpdate.NONE : mapUpdate;
    }

    public static RadarSyncSnapshot of(long power, RadarDetectable.RadarScanParams scanSettings,
            boolean redstoneProximityMode, boolean showMap, boolean jammed, int redstonePower,
            Collection<RadarEntry> entries, RadarMapUpdate mapUpdate) {
        return new RadarSyncSnapshot(power, scanSettings, redstoneProximityMode, showMap, jammed, redstonePower,
                entries != null ? List.copyOf(entries) : List.of(), mapUpdate);
    }

    public CompoundTag toTag(boolean includeEntries) {
        CompoundTag tag = new CompoundTag();
        writeTo(tag, includeEntries);
        return tag;
    }

    public void writeTo(CompoundTag tag, boolean includeEntries) {
        tag.putLong(TAG_POWER, power);
        scanSettings.writeTo(tag);
        redstoneMode().writeTo(tag);
        tag.putBoolean(TAG_SHOW_MAP, showMap);
        tag.putBoolean(TAG_JAMMED, jammed);
        tag.putInt(TAG_LAST_RED_POWER, redstonePower);
        if (includeEntries) {
            tag.put(TAG_ENTRIES, RadarEntry.writeList(entries));
        }
        mapUpdate.writeTo(tag);
    }

    public static RadarSyncSnapshot fromTag(CompoundTag tag) {
        RadarDetectable.RadarScanParams scanSettings = RadarDetectable.RadarScanParams.fromTag(tag);
        List<RadarEntry> entries = tag.contains(TAG_ENTRIES, Tag.TAG_LIST)
                ? RadarEntry.readList(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND))
                : List.of();
        return new RadarSyncSnapshot(
                tag.getLong(TAG_POWER),
                scanSettings,
                RadarRedstoneMode.fromTag(tag).legacyFlag(),
                tag.getBoolean(TAG_SHOW_MAP),
                tag.getBoolean(TAG_JAMMED),
                tag.getInt(TAG_LAST_RED_POWER),
                entries,
                RadarMapUpdate.fromTag(tag));
    }

    public void writeLegacyWire(FriendlyByteBuf buffer) {
        buffer.writeLong(power);
        scanSettings.writeLegacyWire(buffer);
        redstoneMode().writeLegacyWire(buffer);
        buffer.writeBoolean(showMap);
        buffer.writeBoolean(jammed);
        buffer.writeInt(entries.size());
        for (RadarEntry entry : entries) {
            entry.writeLegacyWire(buffer);
        }
        mapUpdate.writeLegacyWire(buffer);
    }

    public static RadarSyncSnapshot readLegacyWire(FriendlyByteBuf buffer) {
        long power = buffer.readLong();
        RadarDetectable.RadarScanParams scanSettings = RadarDetectable.RadarScanParams.readLegacyWire(buffer);
        boolean redstoneProximityMode = RadarRedstoneMode.readLegacyWire(buffer).legacyFlag();
        boolean showMap = buffer.readBoolean();
        boolean jammed = buffer.readBoolean();
        int count = Math.max(0, buffer.readInt());
        java.util.ArrayList<RadarEntry> entries = new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            entries.add(RadarEntry.readLegacyWire(buffer));
        }
        return new RadarSyncSnapshot(power, scanSettings, redstoneProximityMode, showMap, jammed, 0, entries,
                RadarMapUpdate.readLegacyWire(buffer));
    }

    public RadarRedstoneMode redstoneMode() {
        return RadarRedstoneMode.fromLegacyFlag(redstoneProximityMode);
    }
}
