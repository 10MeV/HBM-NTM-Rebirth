package com.hbm.ntm.api.entity;

import java.util.List;

public record RadarScanResult(List<RadarEntry> entries, boolean jammed) {
    public static final RadarScanResult EMPTY = new RadarScanResult(List.of(), false);

    public RadarScanResult {
        entries = List.copyOf(entries);
    }
}
