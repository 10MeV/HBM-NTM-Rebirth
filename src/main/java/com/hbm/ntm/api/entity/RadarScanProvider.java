package com.hbm.ntm.api.entity;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface RadarScanProvider {
    RadarScanResult getScanResultSnapshot();

    default List<RadarEntry> getEntries() {
        return getScanResultSnapshot().entries();
    }

    default int getEntryAmount() {
        return getScanResultSnapshot().amount();
    }

    default Optional<RadarEntry> getEntryAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().entryAtLegacyIndex(legacyIndex);
    }

    default Optional<Boolean> isEntryPlayerAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().isPlayerAtLegacyIndex(legacyIndex);
    }

    default OptionalInt getEntryTypeAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().typeAtLegacyIndex(legacyIndex);
    }

    default Optional<RadarEntry.LegacyEntityInfo> getEntryInfoAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().entityInfoAtLegacyIndex(legacyIndex);
    }
}
