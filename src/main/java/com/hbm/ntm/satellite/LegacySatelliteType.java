package com.hbm.ntm.satellite;

import com.hbm.ntm.itempool.HbmItemPoolIds;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public enum LegacySatelliteType {
    MAPPER(0, "mapper", Satellite.SatelliteInterface.SAT_PANEL),
    SCANNER(1, "scanner", Satellite.SatelliteInterface.SAT_PANEL),
    RADAR(2, "radar", Satellite.SatelliteInterface.SAT_PANEL),
    LASER(3, "laser", Satellite.SatelliteInterface.SAT_PANEL),
    RESONATOR(4, "resonator", Satellite.SatelliteInterface.SAT_COORD),
    RELAY(5, "foeq", Satellite.SatelliteInterface.NONE, "relay"),
    MINER(6, "miner", Satellite.SatelliteInterface.NONE),
    LUNAR_MINER(7, "lunar_miner", Satellite.SatelliteInterface.NONE, "lunar"),
    HORIZONS(8, "gerald", Satellite.SatelliteInterface.SAT_COORD, "horizons");

    private static final List<String> NAMES = Arrays.stream(values())
            .flatMap(type -> type.names.stream())
            .toList();

    private final int legacyId;
    private final String legacyName;
    private final Satellite.SatelliteInterface satelliteInterface;
    private final List<String> names;

    LegacySatelliteType(int legacyId, String legacyName, Satellite.SatelliteInterface satelliteInterface,
            String... aliases) {
        this.legacyId = legacyId;
        this.legacyName = legacyName;
        this.satelliteInterface = satelliteInterface;
        this.names = Stream.concat(Stream.of(legacyName), Arrays.stream(aliases))
                .map(name -> name.toLowerCase(Locale.ROOT))
                .toList();
    }

    public int legacyId() {
        return legacyId;
    }

    public String legacyName() {
        return legacyName;
    }

    public Satellite.SatelliteInterface satelliteInterface() {
        return satelliteInterface;
    }

    public Optional<String> cargoPool() {
        return Satellite.cargoPoolForType(this);
    }

    Optional<String> defaultCargoPool() {
        return switch (this) {
            case MINER -> Optional.of(HbmItemPoolIds.POOL_SAT_MINER);
            case LUNAR_MINER -> Optional.of(HbmItemPoolIds.POOL_SAT_LUNAR);
            default -> Optional.empty();
        };
    }

    public static LegacySatelliteType byLegacyId(int legacyId) {
        for (LegacySatelliteType type : values()) {
            if (type.legacyId == legacyId) {
                return type;
            }
        }
        return null;
    }

    public static LegacySatelliteType byName(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        for (LegacySatelliteType type : values()) {
            if (type.names.contains(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static List<String> names() {
        return NAMES;
    }
}
