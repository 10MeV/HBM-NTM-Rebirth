package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.List;

public final class RadarHostStateProfile {
    public static final String TAG_LEGACY_POWER = "power";
    public static final String TAG_ENTRIES = "Entries";
    public static final String TAG_SHOW_MAP = "showMap";

    private RadarHostStateProfile() {
    }

    public static RadarControlState controlState(RadarDetectable.RadarScanParams scanSettings,
            boolean redstoneProximityMode, boolean showMap) {
        return RadarControlState.of(scanSettings, redstoneProximityMode, showMap);
    }

    public static RadarHostSyncState syncState(long power, boolean jammed, int redstonePower, int entryCount) {
        return new RadarHostSyncState(power, jammed, redstonePower, entryCount);
    }

    public static RadarScanResult scanResult(Collection<RadarEntry> entries, boolean jammed) {
        return new RadarScanResult(entries != null ? List.copyOf(entries) : List.of(), jammed);
    }

    public static RadarStatusSnapshot statusSnapshot(BlockPos pos, int range, long power, long maxPower,
            boolean jammed, int entryAmount, int redstonePower, RadarDetectable.RadarScanParams scanSettings,
            boolean redstoneProximityMode, boolean showMap) {
        return new RadarStatusSnapshot(pos, range, power, maxPower, jammed, entryAmount, redstonePower,
                scanSettings, redstoneProximityMode, showMap);
    }

    public static RadarSyncSnapshot syncSnapshot(long power, RadarDetectable.RadarScanParams scanSettings,
            boolean redstoneProximityMode, boolean showMap, boolean jammed, int redstonePower,
            Collection<RadarEntry> entries, RadarMapUpdate mapUpdate) {
        return RadarSyncSnapshot.of(power, scanSettings, redstoneProximityMode, showMap, jammed, redstonePower,
                entries, mapUpdate);
    }

    public static RadarSyncSnapshot syncSnapshotFromTag(CompoundTag tag) {
        return RadarSyncSnapshot.fromTag(tag);
    }

    /**
     * TileEntityMachineRadarNT persisted only its durable host controls and full
     * terrain map. Jam state, redstone output and scan entries were sent over its
     * live packet every tick and must start clean after a world reload.
     */
    public static void writePersistentState(CompoundTag tag, long power,
            RadarDetectable.RadarScanParams scanSettings, boolean redstoneProximityMode, boolean showMap) {
        if (tag == null) {
            return;
        }
        tag.putLong(TAG_LEGACY_POWER, power);
        (scanSettings != null ? scanSettings : RadarDetectable.RadarScanParams.DEFAULT).writeTo(tag);
        RadarRedstoneMode.fromLegacyFlag(redstoneProximityMode).writeTo(tag);
        tag.putBoolean(TAG_SHOW_MAP, showMap);
    }

    public static PersistentState persistentStateFromTag(CompoundTag tag) {
        RadarDetectable.RadarScanParams legacyMissingValue = new RadarDetectable.RadarScanParams(false, false,
                false, false);
        if (tag == null) {
            return new PersistentState(0L, legacyMissingValue, false, false);
        }
        return new PersistentState(tag.getLong(TAG_LEGACY_POWER),
                RadarDetectable.RadarScanParams.fromTag(tag, legacyMissingValue),
                RadarRedstoneMode.fromTag(tag, RadarRedstoneMode.TIER).legacyFlag(),
                tag.getBoolean(TAG_SHOW_MAP));
    }

    public static boolean hasLegacyPower(CompoundTag tag) {
        return tag != null && tag.contains(TAG_LEGACY_POWER, Tag.TAG_LONG);
    }

    public static boolean hasEntries(CompoundTag tag) {
        return tag != null && tag.contains(TAG_ENTRIES, Tag.TAG_LIST);
    }

    public static List<RadarEntry> entriesFromTag(CompoundTag tag) {
        return hasEntries(tag) ? RadarEntry.readList(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND)) : List.of();
    }

    public static void writeSyncSnapshot(CompoundTag tag, RadarSyncSnapshot snapshot, boolean includeEntries) {
        if (tag != null && snapshot != null) {
            snapshot.writeTo(tag, includeEntries);
        }
    }

    public static void writeFullMap(CompoundTag tag, byte[] map) {
        if (tag != null) {
            RadarMap.writeTo(tag, map);
        }
    }

    public static byte[] fullMapFromTag(CompoundTag tag, byte[] map) {
        return RadarMapUpdate.fromTag(tag).applyTo(RadarMap.readFrom(tag, map));
    }

    public static AppliedSyncState applySyncSnapshot(RadarSyncSnapshot snapshot, byte[] map, boolean applyPower,
            boolean includeEntries) {
        RadarSyncSnapshot safeSnapshot = snapshot != null
                ? snapshot
                : syncSnapshot(0L, RadarDetectable.RadarScanParams.DEFAULT, true, false, false, 0, List.of(),
                        RadarMapUpdate.NONE);
        return new AppliedSyncState(applyPower, safeSnapshot.power(), safeSnapshot.scanSettings(),
                safeSnapshot.redstoneProximityMode(), safeSnapshot.showMap(), safeSnapshot.jammed(),
                safeSnapshot.redstonePower(), includeEntries, includeEntries ? safeSnapshot.entries() : List.of(),
                safeSnapshot.mapUpdate().applyTo(map));
    }

    public record AppliedSyncState(boolean applyPower, long power, RadarDetectable.RadarScanParams scanSettings,
                                   boolean redstoneProximityMode, boolean showMap, boolean jammed,
                                   int redstonePower, boolean includeEntries, List<RadarEntry> entries,
                                   byte[] map) {
        public AppliedSyncState {
            scanSettings = scanSettings == null ? RadarDetectable.RadarScanParams.DEFAULT : scanSettings;
            entries = List.copyOf(entries != null ? entries : List.of());
            map = RadarMap.normalize(map);
        }
    }

    public record PersistentState(long power, RadarDetectable.RadarScanParams scanSettings,
                                  boolean redstoneProximityMode, boolean showMap) {
        public PersistentState {
            scanSettings = scanSettings != null ? scanSettings : RadarDetectable.RadarScanParams.DEFAULT;
        }
    }
}
