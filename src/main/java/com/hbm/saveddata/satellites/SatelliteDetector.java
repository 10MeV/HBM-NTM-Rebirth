package com.hbm.saveddata.satellites;

import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.satellite.LegacySatelliteType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Source-backed wideband-emission Detector satellite and its transient event table. */
public final class SatelliteDetector extends SatelliteBase {
    public static final String CMD_SURVEY = "survey";
    public static final String CMD_COUNT = "count";
    public static final String CMD_GETTYPE = "gettype";
    public static final String CMD_GETPOSITION = "getposition";
    public static final int DURATION_LOW = 15 * 20;
    public static final int DURATION_MEDIUM = 20 / 2;
    public static final int DURATION_HIGH = 60 * 20;
    public static final double INACCURACY_LOW = 10_000.0D;
    public static final double INACCURACY_MEDIUM = 2_500.0D;
    public static final double INACCURACY_HIGH = 500.0D;

    private static final List<RadiationBurst> BURSTS = new ArrayList<>();
    private final List<RadiationBurst> cachedResults = new ArrayList<>();

    @Override public LegacySatelliteType type() { return LegacySatelliteType.DETECTOR; }
    @Override public String getType() { return "UWB_EMISSION_DETECTOR"; }

    @Override
    public void onCommandImpl(ServerLevel level, String... command) {
        if (command == null || command.length == 0) return;
        if (CMD_SURVEY.equals(command[0])) {
            cachedResults.clear();
            for (RadiationBurst burst : BURSTS) if (burst.dimension.equals(level.dimension())) cachedResults.add(burst);
            return;
        }
        if (CMD_COUNT.equals(command[0])) {
            tx = Integer.toString(cachedResults.size());
            return;
        }
        if (CMD_GETTYPE.equals(command[0]) && command.length == 2) {
            RadiationBurst burst = burstFromIndex(command[1]);
            tx = burst == null ? "" : burst.intensity.name();
            return;
        }
        if (CMD_GETPOSITION.equals(command[0]) && command.length == 2) {
            RadiationBurst burst = burstFromIndex(command[1]);
            tx = burst == null ? "" : burst.x + ";" + burst.z;
        }
    }

    private RadiationBurst burstFromIndex(String value) {
        if (cachedResults.isEmpty()) return null;
        return cachedResults.get(RORInteractive.parseInt(value, 1, cachedResults.size()) - 1);
    }

    public static void reportEvent(Level level, int lifetime, BurstIntensity intensity, double x, double z) {
        if (level == null || intensity == null) return;
        double inaccuracy = switch (intensity) {
            case LOW -> INACCURACY_LOW;
            case MEDIUM -> INACCURACY_MEDIUM;
            case HIGH -> INACCURACY_HIGH;
        };
        BURSTS.add(new RadiationBurst(level.dimension(), level.getGameTime() + lifetime, intensity,
                (int) Math.floor(x) + (int) (level.getRandom().nextGaussian() * inaccuracy),
                (int) Math.floor(z) + (int) (level.getRandom().nextGaussian() * inaccuracy)));
    }

    public static void updateSystem(Level level) {
        if (level == null) return;
        Iterator<RadiationBurst> iterator = BURSTS.iterator();
        while (iterator.hasNext()) {
            RadiationBurst burst = iterator.next();
            if (burst.dimension.equals(level.dimension()) && level.getGameTime() > burst.expiresOn) iterator.remove();
        }
    }

    /** Releases process-local events for a server level that is being unloaded. */
    public static void unloadLevel(Level level) {
        if (level != null) {
            BURSTS.removeIf(burst -> burst.dimension.equals(level.dimension()));
        }
    }

    /** Releases every process-local event when the server session ends. */
    public static void clearAll() {
        BURSTS.clear();
    }

    public enum BurstIntensity { LOW, MEDIUM, HIGH }

    private record RadiationBurst(ResourceKey<Level> dimension, long expiresOn, BurstIntensity intensity, int x, int z) { }
}
