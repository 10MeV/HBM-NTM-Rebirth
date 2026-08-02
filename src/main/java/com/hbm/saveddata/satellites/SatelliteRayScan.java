package com.hbm.saveddata.satellites;

import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.satellite.LegacySatelliteType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Transient emission registry used by the legacy Ray Scan satellite.
 *
 * <p>The entries deliberately stay in insertion order and are not SavedData: the
 * 1.7.10 implementation kept the same process-local table and only expired it
 * from the level tick.</p>
 */
public final class SatelliteRayScan extends SatelliteBase {
    public static final int MAX_SCAN_RANGE = 250;
    public static final String CMD_SURVEY = "survey";
    public static final String CMD_COUNT = "count";
    public static final String CMD_GETINFO = "getinfo";
    public static final String CMD_GETPOSITION = "getposition";

    private static final LinkedHashMap<EventPosition, RayEvent> RAY_EVENTS = new LinkedHashMap<>();

    private final List<RayEvent> cachedResults = new ArrayList<>();

    @Override public LegacySatelliteType type() { return LegacySatelliteType.RAY_SCAN; }
    @Override public String getType() { return "NB_RAY_SCANNER"; }

    @Override
    public void onCommandImpl(net.minecraft.server.level.ServerLevel level, String... command) {
        if (command == null || command.length == 0) return;
        if (CMD_SURVEY.equals(command[0])) {
            cachedResults.clear();
            for (Map.Entry<EventPosition, RayEvent> entry : RAY_EVENTS.entrySet()) {
                EventPosition position = entry.getKey();
                if (!position.dimension.equals(level.dimension())) continue;
                int deltaX = position.x - targetX;
                int deltaZ = position.z - targetZ;
                if (deltaX * deltaX + deltaZ * deltaZ <= MAX_SCAN_RANGE * MAX_SCAN_RANGE) {
                    cachedResults.add(entry.getValue());
                }
            }
            return;
        }
        if (CMD_COUNT.equals(command[0])) {
            tx = Integer.toString(cachedResults.size());
            return;
        }
        if (CMD_GETINFO.equals(command[0]) && command.length == 2) {
            RayEvent event = eventFromIndex(command[1]);
            tx = event == null ? "" : event.info;
            return;
        }
        if (CMD_GETPOSITION.equals(command[0]) && command.length == 2) {
            RayEvent event = eventFromIndex(command[1]);
            tx = event == null ? "" : event.x + ";" + event.z;
        }
    }

    private RayEvent eventFromIndex(String value) {
        if (cachedResults.isEmpty()) return null;
        return cachedResults.get(RORInteractive.parseInt(value, 1, cachedResults.size()) - 1);
    }

    public static void reportEvent(Level level, int x, int y, int z, String info, int lifetime) {
        RAY_EVENTS.put(new EventPosition(level.dimension(), x, y, z),
                new RayEvent(level.getGameTime() + lifetime, x, z, info));
    }

    /** Mirrors the old level-tick expiry check; callers preserve its once-per-second cadence. */
    public static void updateSystem(Level level) {
        Iterator<Map.Entry<EventPosition, RayEvent>> iterator = RAY_EVENTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EventPosition, RayEvent> entry = iterator.next();
            if (entry.getKey().dimension().equals(level.dimension())
                    && level.getGameTime() > entry.getValue().expiresOn()) {
                iterator.remove();
            }
        }
    }

    /** Releases process-local events for a server level that is being unloaded. */
    public static void unloadLevel(Level level) {
        if (level != null) {
            RAY_EVENTS.entrySet().removeIf(entry -> entry.getKey().dimension().equals(level.dimension()));
        }
    }

    /** Releases every process-local event when the server session ends. */
    public static void clearAll() {
        RAY_EVENTS.clear();
    }

    public static final class RayEvent {
        public static final String INFO_ARC_FLASH = "ARC_FLASH";
        public static final String INFO_NUCLEAR = "NEUTRON_EMISSION";
        public static final String INFO_PARTICLE = "HIGH_ENERGY_PARTICLES";
        public static final String INFO_RADAR = "RADAR_WAVES";
        public static final String INFO_RADIO = "RADIO_WAVES";

        private final long expiresOn;
        private final String info;
        private final int x;
        private final int z;

        private RayEvent(long expiresOn, int x, int z, String info) {
            this.expiresOn = expiresOn;
            this.info = info;
            this.x = x;
            this.z = z;
        }

        public long expiresOn() { return expiresOn; }
        public String info() { return info; }
        public int x() { return x; }
        public int z() { return z; }
    }

    private record EventPosition(ResourceKey<Level> dimension, int x, int y, int z) {
    }
}
