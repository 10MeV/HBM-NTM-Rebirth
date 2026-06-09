package com.hbm.ntm.api.entity;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public record RadarScanResult(List<RadarEntry> entries, boolean jammed) {
    public static final RadarScanResult EMPTY = new RadarScanResult(List.of(), false);

    public RadarScanResult {
        entries = List.copyOf(entries);
    }

    public int amount() {
        return entries.size();
    }

    public Optional<RadarEntry> entryAtLegacyIndex(int legacyIndex) {
        int index = legacyIndex - 1;
        if (index < 0 || index >= entries.size()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(index));
    }

    public Optional<Boolean> isPlayerAtLegacyIndex(int legacyIndex) {
        return entryAtLegacyIndex(legacyIndex).map(RadarEntry::isPlayer);
    }

    public OptionalInt typeAtLegacyIndex(int legacyIndex) {
        Optional<RadarEntry> entry = entryAtLegacyIndex(legacyIndex);
        return entry.map(radarEntry -> OptionalInt.of(radarEntry.legacyType())).orElseGet(OptionalInt::empty);
    }

    public Optional<RadarEntry.LegacyEntityInfo> entityInfoAtLegacyIndex(int legacyIndex) {
        return entryAtLegacyIndex(legacyIndex).map(RadarEntry::legacyEntityInfo);
    }
}
